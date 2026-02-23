import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, Trash2, RotateCcw } from "lucide-react";
import { backlogApi, type BacklogEntry, type BacklogStatus, type BacklogUpdateRequest } from "../api/backlog";
import { gamesApi, type IgdbSearchResult } from "../api/games";
import { Modal } from "../components/ui/Modal";
import { Button } from "../components/ui/Button";

const STATUS_TABS: { value: BacklogStatus | "ALL"; label: string }[] = [
  { value: "ALL", label: "All" },
  { value: "WANT_TO_PLAY", label: "Want to Play" },
  { value: "PLAYING", label: "Playing" },
  { value: "COMPLETED", label: "Completed" },
  { value: "DROPPED", label: "Dropped" },
];

const STATUS_LABELS: Record<BacklogStatus, string> = {
  WANT_TO_PLAY: "Want to Play",
  PLAYING: "Playing",
  COMPLETED: "Completed",
  DROPPED: "Dropped",
};

const STATUS_COLORS: Record<BacklogStatus, string> = {
  WANT_TO_PLAY: "bg-blue-50 text-blue-700",
  PLAYING: "bg-green-50 text-green-700",
  COMPLETED: "bg-purple-50 text-purple-700",
  DROPPED: "bg-gray-100 text-gray-600",
};

function CoverPlaceholder() {
  return (
    <div className="w-16 h-20 rounded-lg bg-gray-100 shrink-0 flex items-center justify-center">
      <span className="text-gray-300 text-xl">🎮</span>
    </div>
  );
}

// ---- Add Modal (2-step) ----

interface AddModalProps {
  open: boolean;
  onClose: () => void;
}

function AddModal({ open, onClose }: AddModalProps) {
  const queryClient = useQueryClient();
  const [step, setStep] = useState<"search" | "configure">("search");
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState<IgdbSearchResult[]>([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);
  const [hasSearched, setHasSearched] = useState(false);
  const [selected, setSelected] = useState<IgdbSearchResult | null>(null);
  const [backlogStatus, setBacklogStatus] = useState<BacklogStatus>("WANT_TO_PLAY");
  const [rating, setRating] = useState<string>("");
  const [notes, setNotes] = useState("");

  const addMutation = useMutation({
    mutationFn: backlogApi.add,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["backlog"] });
      handleClose();
    },
  });

  function handleClose() {
    onClose();
    setStep("search");
    setQuery("");
    setSearchResults([]);
    setSearchError(null);
    setHasSearched(false);
    setSelected(null);
    setBacklogStatus("WANT_TO_PLAY");
    setRating("");
    setNotes("");
  }

  async function handleSearch() {
    if (!query.trim()) return;
    setSearching(true);
    setSearchError(null);
    setHasSearched(true);
    try {
      const results = await gamesApi.lookupSearch(query.trim());
      setSearchResults(results);
    } catch {
      setSearchError("Search failed. Check that the backend is running.");
      setSearchResults([]);
    } finally {
      setSearching(false);
    }
  }

  function handleSelect(result: IgdbSearchResult) {
    setSelected(result);
    setStep("configure");
  }

  function handleAdd() {
    if (!selected) return;
    const ratingNum = rating ? parseInt(rating, 10) : undefined;
    addMutation.mutate({
      igdbId: selected.igdbId,
      name: selected.title,
      coverUrl: selected.imageUrl ?? undefined,
      releaseDate: selected.releaseDate ?? undefined,
      backlogStatus,
      igdbScore: selected.igdbScore ?? null,
      rating: ratingNum || undefined,
      notes: notes || undefined,
    });
  }

  return (
    <Modal open={open} onClose={handleClose} title="Add to Backlog">
      {step === "search" ? (
        <div className="flex flex-col gap-4">
          <div className="flex gap-2">
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              placeholder="Search IGDB..."
              className="flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
            />
            <Button onClick={handleSearch} loading={searching} size="sm">
              Search
            </Button>
          </div>

          {searchResults.length > 0 && (
            <div className="flex flex-col gap-1 max-h-80 overflow-y-auto -mx-1 px-1">
              {searchResults.map((r) => (
                <button
                  key={r.igdbId}
                  onClick={() => handleSelect(r)}
                  className="flex items-center gap-3 rounded-lg p-2 text-left hover:bg-gray-50 transition-colors"
                >
                  {r.imageUrl ? (
                    <img
                      src={r.imageUrl}
                      alt={r.title}
                      className="w-10 h-14 rounded object-cover shrink-0"
                    />
                  ) : (
                    <div className="w-10 h-14 rounded bg-gray-100 shrink-0" />
                  )}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{r.title}</p>
                    {r.releaseDate && (
                      <p className="text-xs text-gray-500">{r.releaseDate.slice(0, 4)}</p>
                    )}
                  </div>
                </button>
              ))}
            </div>
          )}

          {searchError && (
            <p className="text-sm text-red-500 text-center py-2">{searchError}</p>
          )}

          {!searchError && hasSearched && !searching && searchResults.length === 0 && (
            <p className="text-sm text-gray-400 text-center py-4">No results found.</p>
          )}
        </div>
      ) : (
        <div className="flex flex-col gap-5">
          {selected && (
            <div className="flex items-center gap-3 p-3 rounded-lg bg-gray-50 border border-gray-200">
              {selected.imageUrl ? (
                <img
                  src={selected.imageUrl}
                  alt={selected.title}
                  className="w-10 h-14 rounded object-cover shrink-0"
                />
              ) : (
                <div className="w-10 h-14 rounded bg-gray-200 shrink-0" />
              )}
              <div>
                <p className="text-sm font-semibold text-gray-900">{selected.title}</p>
                {selected.releaseDate && (
                  <p className="text-xs text-gray-500">{selected.releaseDate.slice(0, 4)}</p>
                )}
              </div>
            </div>
          )}

          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium text-gray-700">Status</label>
            <select
              value={backlogStatus}
              onChange={(e) => setBacklogStatus(e.target.value as BacklogStatus)}
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
            >
              {(Object.keys(STATUS_LABELS) as BacklogStatus[]).map((s) => (
                <option key={s} value={s}>{STATUS_LABELS[s]}</option>
              ))}
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium text-gray-700">Rating (1–10, optional)</label>
            <input
              type="number"
              min="1"
              max="10"
              value={rating}
              onChange={(e) => setRating(e.target.value)}
              placeholder="—"
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 w-24"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs font-medium text-gray-700">Notes (optional)</label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
              placeholder="Your thoughts..."
              className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 resize-none"
            />
          </div>

          <div className="flex justify-end gap-2 pt-1">
            <Button variant="secondary" size="sm" onClick={() => setStep("search")}>
              Back
            </Button>
            <Button size="sm" onClick={handleAdd} loading={addMutation.isPending}>
              Add to Backlog
            </Button>
          </div>

          {addMutation.isError && (
            <p className="text-xs text-red-500 text-center">
              {(addMutation.error as Error)?.message ?? "Failed to add game."}
            </p>
          )}
        </div>
      )}
    </Modal>
  );
}

// ---- Edit Modal ----

interface EditModalProps {
  entry: BacklogEntry;
  onClose: () => void;
}

function EditModal({ entry, onClose }: EditModalProps) {
  const queryClient = useQueryClient();
  const [backlogStatus, setBacklogStatus] = useState<BacklogStatus>(entry.backlogStatus);
  const [rating, setRating] = useState<string>(entry.rating != null ? String(entry.rating) : "");
  const [notes, setNotes] = useState<string>(entry.notes ?? "");

  const updateMutation = useMutation({
    mutationFn: (data: BacklogUpdateRequest) => backlogApi.update(entry.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["backlog"] });
      onClose();
    },
  });

  function handleSave() {
    const ratingNum = rating ? parseInt(rating, 10) : null;
    updateMutation.mutate({
      backlogStatus,
      rating: ratingNum,
      notes: notes || null,
    });
  }

  return (
    <Modal open onClose={onClose} title="Edit Entry">
      <div className="flex flex-col gap-5">
        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-gray-700">Status</label>
          <select
            value={backlogStatus}
            onChange={(e) => setBacklogStatus(e.target.value as BacklogStatus)}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500"
          >
            {(Object.keys(STATUS_LABELS) as BacklogStatus[]).map((s) => (
              <option key={s} value={s}>{STATUS_LABELS[s]}</option>
            ))}
          </select>
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-gray-700">Rating (1–10, optional)</label>
          <input
            type="number"
            min="1"
            max="10"
            value={rating}
            onChange={(e) => setRating(e.target.value)}
            placeholder="—"
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 w-24"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-xs font-medium text-gray-700">Notes (optional)</label>
          <textarea
            value={notes}
            onChange={(e) => setNotes(e.target.value)}
            rows={3}
            className="rounded-lg border border-gray-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 resize-none"
          />
        </div>

        <div className="flex justify-end gap-2 pt-1">
          <Button variant="secondary" size="sm" onClick={onClose}>Cancel</Button>
          <Button size="sm" onClick={handleSave} loading={updateMutation.isPending}>Save</Button>
        </div>

        {updateMutation.isError && (
          <p className="text-xs text-red-500 text-center">Failed to save changes.</p>
        )}
      </div>
    </Modal>
  );
}

// ---- Backlog Card ----

interface BacklogCardProps {
  entry: BacklogEntry;
}

function ScoreBadge({ score }: { score: number | null }) {
  if (score == null) {
    return <span className="text-sm font-medium text-gray-300 w-8 text-center">—</span>;
  }
  const colorClass =
    score >= 75
      ? "bg-green-100 text-green-700"
      : score >= 50
      ? "bg-yellow-100 text-yellow-700"
      : "bg-red-100 text-red-700";
  return (
    <span className={`inline-block rounded-md px-1.5 py-0.5 text-xs font-bold tabular-nums ${colorClass}`}>
      {score}
    </span>
  );
}

function BacklogCard({ entry }: BacklogCardProps) {
  const queryClient = useQueryClient();
  const [editOpen, setEditOpen] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const deleteMutation = useMutation({
    mutationFn: () => backlogApi.delete(entry.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["backlog"] });
    },
  });

  async function handleRefreshScore() {
    if (!entry.igdbId || refreshing) return;
    setRefreshing(true);
    try {
      const detail = await gamesApi.lookupDetail(entry.igdbId);
      if (detail) {
        await backlogApi.update(entry.id, { igdbScore: detail.igdbScore });
        queryClient.invalidateQueries({ queryKey: ["backlog"] });
      }
    } finally {
      setRefreshing(false);
    }
  }

  const releaseYear = entry.releaseDate ? entry.releaseDate.slice(0, 4) : null;

  return (
    <>
      <div className="group flex items-start gap-4 px-5 py-4 border-b border-gray-100 last:border-0">
        {entry.coverUrl ? (
          <img
            src={entry.coverUrl}
            alt={entry.name}
            className="w-16 h-20 rounded-lg object-cover shrink-0"
          />
        ) : (
          <CoverPlaceholder />
        )}

        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold text-gray-900 truncate">{entry.name}</p>
          {releaseYear && (
            <p className="text-xs text-gray-400 mt-0.5">{releaseYear}</p>
          )}
          <div className="flex items-center gap-2 mt-2 flex-wrap">
            <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[entry.backlogStatus]}`}>
              {STATUS_LABELS[entry.backlogStatus]}
            </span>
            {entry.rating != null && (
              <span className="text-xs text-gray-500 font-medium">{entry.rating}/10</span>
            )}
          </div>
          {entry.notes && (
            <p className="mt-2 text-xs text-gray-500 line-clamp-2">{entry.notes}</p>
          )}
        </div>

        <div className="flex flex-col items-end gap-2 shrink-0 pt-0.5">
          <ScoreBadge score={entry.igdbScore} />
          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={handleRefreshScore}
              disabled={refreshing}
              className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
              title="Refresh critic score"
            >
              <RotateCcw size={14} className={refreshing ? "animate-spin" : ""} />
            </button>
            <button
              onClick={() => setEditOpen(true)}
              className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
              title="Edit"
            >
              <Pencil size={14} />
            </button>
            <button
              onClick={() => setConfirmDelete(true)}
              className="rounded-lg p-1.5 text-gray-400 hover:bg-red-50 hover:text-red-500 transition-colors"
              title="Delete"
            >
              <Trash2 size={14} />
            </button>
          </div>
        </div>
      </div>

      {editOpen && <EditModal entry={entry} onClose={() => setEditOpen(false)} />}

      {confirmDelete && (
        <Modal open onClose={() => setConfirmDelete(false)} title="Remove from Backlog">
          <div className="flex flex-col gap-4">
            <p className="text-sm text-gray-600">
              Remove <strong>{entry.name}</strong> from your backlog?
            </p>
            <div className="flex justify-end gap-2">
              <Button variant="secondary" size="sm" onClick={() => setConfirmDelete(false)}>
                Cancel
              </Button>
              <Button
                variant="danger"
                size="sm"
                loading={deleteMutation.isPending}
                onClick={() => deleteMutation.mutateAsync().then(() => setConfirmDelete(false))}
              >
                Remove
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
}

// ---- Main Page ----

export function BacklogPage() {
  const [activeTab, setActiveTab] = useState<BacklogStatus | "ALL">("ALL");
  const [addOpen, setAddOpen] = useState(false);

  const { data: entries = [], isLoading, isError } = useQuery({
    queryKey: ["backlog"],
    queryFn: () => backlogApi.list(),
  });

  const tabCounts = useMemo(() => {
    const counts: Record<string, number> = { ALL: entries.length };
    for (const e of entries) {
      counts[e.backlogStatus] = (counts[e.backlogStatus] ?? 0) + 1;
    }
    return counts;
  }, [entries]);

  const filtered = useMemo(
    () => (activeTab === "ALL" ? entries : entries.filter((e) => e.backlogStatus === activeTab)),
    [entries, activeTab]
  );

  return (
    <div className="flex flex-col gap-8">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-gray-900">Backlog</h1>
        <Button size="sm" onClick={() => setAddOpen(true)}>
          <Plus size={14} />
          Add to Backlog
        </Button>
      </div>

      {/* Status tabs */}
      <div className="flex gap-1 border-b border-gray-200 overflow-x-auto">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setActiveTab(tab.value)}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition-colors whitespace-nowrap ${
              activeTab === tab.value
                ? "border-brand-500 text-brand-600"
                : "border-transparent text-gray-500 hover:text-gray-700"
            }`}
          >
            {tab.label}
            {tabCounts[tab.value] != null && tabCounts[tab.value] > 0 && (
              <span className="ml-1.5 text-xs text-gray-400">
                {tabCounts[tab.value]}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
          {Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="flex items-start gap-4 px-5 py-4 border-b border-gray-100 last:border-0">
              <div className="w-16 h-20 rounded-lg bg-gray-100 animate-pulse shrink-0" />
              <div className="flex-1 flex flex-col gap-2 pt-1">
                <div className="h-4 w-3/4 rounded bg-gray-100 animate-pulse" />
                <div className="h-3 w-1/4 rounded bg-gray-100 animate-pulse" />
                <div className="h-5 w-24 rounded-full bg-gray-100 animate-pulse mt-1" />
              </div>
            </div>
          ))}
        </div>
      )}

      {isError && (
        <p className="py-8 text-center text-sm text-red-500">
          Failed to load backlog. Is the backend running?
        </p>
      )}

      {/* Empty state */}
      {!isLoading && !isError && filtered.length === 0 && (
        <p className="py-12 text-center text-sm text-gray-400">
          {activeTab === "ALL"
            ? "Your backlog is empty. Add a game to get started."
            : `No games with status "${STATUS_LABELS[activeTab as BacklogStatus]}".`}
        </p>
      )}

      {/* Cards */}
      {!isLoading && filtered.length > 0 && (
        <div className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
          {filtered.map((entry) => (
            <BacklogCard key={entry.id} entry={entry} />
          ))}
        </div>
      )}

      <AddModal open={addOpen} onClose={() => setAddOpen(false)} />
    </div>
  );
}
