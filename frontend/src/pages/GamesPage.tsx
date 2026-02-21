import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { format } from "date-fns";
import { gamesApi, type GameFormData, type Platform, type GameStatus } from "../api/games";
import { GameRow } from "../components/GameRow";
import { GameForm } from "../components/GameForm";
import { Modal } from "../components/ui/Modal";
import { Button } from "../components/ui/Button";

const STATUS_TABS: { value: GameStatus; label: string }[] = [
  { value: "UPCOMING",  label: "Upcoming"  },
  { value: "RELEASED",  label: "Released"  },
  { value: "CANCELLED", label: "Cancelled" },
];

const PLATFORM_CHIPS: { value: Platform; label: string }[] = [
  { value: "PC",     label: "PC"     },
  { value: "PS5",    label: "PS5"    },
  { value: "XBOX",   label: "Xbox"   },
  { value: "SWITCH", label: "Switch" },
];

export function GamesPage() {
  const queryClient = useQueryClient();

  const [status, setStatus] = useState<GameStatus>("UPCOMING");
  const [activePlatforms, setActivePlatforms] = useState<Platform[]>([]);
  const [addOpen, setAddOpen] = useState(false);

  const { data, isLoading, isError } = useQuery({
    queryKey: ["games", status],
    queryFn: () => gamesApi.list({ status, size: 200, sort: "releaseDate,asc" }),
  });

  const createGame = useMutation({
    mutationFn: (data: GameFormData) => gamesApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["games"] });
      setAddOpen(false);
    },
  });

  function togglePlatform(p: Platform) {
    setActivePlatforms((prev) =>
      prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]
    );
  }

  const grouped = useMemo(() => {
    if (!data?.content) return [];

    const games =
      activePlatforms.length === 0
        ? data.content
        : data.content.filter((g) =>
            activePlatforms.some((p) => g.platforms.includes(p))
          );

    const map = new Map<string, { label: string; games: typeof games }>();
    for (const game of games) {
      const year = game.releaseDate.slice(0, 4);
      const key = game.tba ? `tba-${year}` : format(new Date(game.releaseDate), "yyyy-MM");
      const label = game.tba ? `${year} — TBA` : format(new Date(game.releaseDate), "MMMM yyyy");
      if (!map.has(key)) map.set(key, { label, games: [] });
      map.get(key)!.games.push(game);
    }
    return [...map.entries()].map(([key, v]) => ({ key, ...v }));
  }, [data, activePlatforms]);

  return (
    <div className="flex flex-col gap-8">

      {/* Status tabs */}
      <div className="flex gap-1 border-b border-gray-200">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setStatus(tab.value)}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 -mb-px transition-colors ${
              status === tab.value
                ? "border-brand-500 text-brand-600"
                : "border-transparent text-gray-500 hover:text-gray-700"
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Platform chips + Add game */}
      <div className="flex items-center justify-between">
        <div className="flex gap-2">
          {PLATFORM_CHIPS.map((p) => (
            <button
              key={p.value}
              onClick={() => togglePlatform(p.value)}
              className={`rounded-full border px-3 py-1 text-xs font-medium transition-colors ${
                activePlatforms.includes(p.value)
                  ? "border-brand-500 bg-brand-50 text-brand-600"
                  : "border-gray-300 bg-white text-gray-600 hover:border-gray-400 hover:text-gray-800"
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>

        <Button size="sm" onClick={() => setAddOpen(true)}>
          <Plus size={14} />
          Add game
        </Button>
      </div>

      {/* Skeletons */}
      {isLoading && (
        <div className="flex flex-col gap-8">
          {[1, 2].map((s) => (
            <div key={s} className="flex flex-col gap-3">
              <div className="h-3 w-24 rounded-full bg-gray-200 animate-pulse" />
              <div className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
                {Array.from({ length: 3 }).map((_, i) => (
                  <div key={i} className="flex items-start gap-4 px-5 py-4">
                    <div className="w-28 h-28 rounded-xl bg-gray-100 animate-pulse shrink-0" />
                    <div className="flex-1 flex flex-col gap-2 pt-1">
                      <div className="h-4 w-3/4 rounded bg-gray-100 animate-pulse" />
                      <div className="h-3 w-1/2 rounded bg-gray-100 animate-pulse" />
                      <div className="h-3 w-1/3 rounded bg-gray-100 animate-pulse" />
                    </div>
                    <div className="flex flex-col items-end gap-2 pt-1">
                      <div className="h-5 w-14 rounded bg-gray-100 animate-pulse" />
                      <div className="h-3 w-10 rounded bg-gray-100 animate-pulse" />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      {isError && (
        <p className="py-8 text-center text-sm text-red-500">
          Failed to load games. Is the backend running?
        </p>
      )}

      {!isLoading && grouped.length === 0 && (
        <p className="py-12 text-center text-sm text-gray-400">No games found.</p>
      )}

      {/* Month groups */}
      <div className="flex flex-col gap-8">
        {grouped.map((group) => (
          <section key={group.key}>
            {/* Month header */}
            <div className="flex items-center gap-3 mb-3">
              <span className="text-xs font-semibold text-gray-500 shrink-0 uppercase tracking-wide">
                {group.label}
              </span>
              <div className="flex-1 h-px bg-gray-200" />
            </div>

            {/* Card — no dividers, whitespace only */}
            <div className="rounded-2xl border border-gray-100 bg-white shadow-sm overflow-hidden">
              {group.games.map((game) => (
                <GameRow key={game.id} game={game} />
              ))}
            </div>
          </section>
        ))}
      </div>

      <Modal open={addOpen} onClose={() => setAddOpen(false)} title="Add game">
        <GameForm
          onSubmit={(data) => createGame.mutateAsync(data).then(() => {})}
          onCancel={() => setAddOpen(false)}
        />
      </Modal>
    </div>
  );
}
