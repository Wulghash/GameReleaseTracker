import { useState, useEffect, useRef, type FormEvent } from "react";
import type { Game, GameFormData, Platform, IgdbSearchResult } from "../api/games";
import { gamesApi } from "../api/games";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";

const PLATFORMS: Platform[] = ["PC", "PS5", "XBOX", "SWITCH"];
const PLATFORM_LABELS: Record<Platform, string> = {
  PC: "PC", PS5: "PS5", XBOX: "Xbox", SWITCH: "Switch",
};

interface GameFormProps {
  initial?: Game;
  onSubmit: (data: GameFormData) => Promise<void>;
  onCancel: () => void;
}

export function GameForm({ initial, onSubmit, onCancel }: GameFormProps) {
  const [title, setTitle] = useState(initial?.title ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [releaseDate, setReleaseDate] = useState(initial?.releaseDate ?? "");
  const [platforms, setPlatforms] = useState<Platform[]>(initial?.platforms ?? []);
  const [shopUrl, setShopUrl] = useState(initial?.shopUrl ?? "");
  const [imageUrl, setImageUrl] = useState(initial?.imageUrl ?? "");
  const [developer, setDeveloper] = useState(initial?.developer ?? "");
  const [publisher, setPublisher] = useState(initial?.publisher ?? "");
  const [igdbId, setIgdbId] = useState<number | undefined>(initial?.igdbId ?? undefined);
  const [tba, setTba] = useState(initial?.tba ?? false);
  const [releaseYear, setReleaseYear] = useState(
    initial?.tba ? initial.releaseDate.slice(0, 4) : String(new Date().getFullYear())
  );
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  // IGDB search state
  const [searchResults, setSearchResults] = useState<IgdbSearchResult[]>([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [prefilled, setPrefilled] = useState(false);
  const [prefillLoading, setPrefillLoading] = useState(false);

  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const suppressSearch = useRef(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  // Close dropdown on outside click
  useEffect(() => {
    function handleMouseDown(e: MouseEvent) {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target as Node)) {
        setShowDropdown(false);
      }
    }
    document.addEventListener("mousedown", handleMouseDown);
    return () => document.removeEventListener("mousedown", handleMouseDown);
  }, []);

  // Debounced IGDB search — only for new games (not edit form)
  useEffect(() => {
    if (initial) return;
    if (suppressSearch.current) {
      suppressSearch.current = false;
      return;
    }

    if (debounceRef.current) clearTimeout(debounceRef.current);

    const trimmed = title.trim();
    if (trimmed.length < 2) {
      setSearchResults([]);
      setShowDropdown(false);
      return;
    }

    debounceRef.current = setTimeout(async () => {
      try {
        const results = await gamesApi.lookupSearch(trimmed);
        setSearchResults(results);
        setShowDropdown(results.length > 0);
      } catch {
        setSearchResults([]);
        setShowDropdown(false);
      }
    }, 400);

    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [title, initial]);

  async function selectResult(result: IgdbSearchResult) {
    setShowDropdown(false);
    setPrefillLoading(true);
    setIgdbId(result.igdbId);
    try {
      const detail = await gamesApi.lookupDetail(result.igdbId);
      suppressSearch.current = true;
      setTitle(detail.title ?? result.title);
      if (detail.releaseDate) {
        setReleaseDate(detail.releaseDate);
        setTba(false);
      } else {
        setTba(true);
      }
      if (detail.platforms.length > 0) setPlatforms(detail.platforms);
      if (detail.description) setDescription(detail.description);
      if (detail.developer) setDeveloper(detail.developer);
      if (detail.publisher) setPublisher(detail.publisher);
      if (detail.imageUrl) setImageUrl(detail.imageUrl);
      setPrefilled(true);
    } catch {
      // Fallback: prefill what we got from search
      suppressSearch.current = true;
      setTitle(result.title);
      if (result.releaseDate) setReleaseDate(result.releaseDate);
      if (result.platforms.length > 0) setPlatforms(result.platforms);
      if (result.imageUrl) setImageUrl(result.imageUrl);
      setPrefilled(true);
    } finally {
      setPrefillLoading(false);
    }
  }

  function togglePlatform(p: Platform) {
    setPlatforms((prev) =>
      prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]
    );
  }

  function validate(): boolean {
    const e: Record<string, string> = {};
    if (!title.trim()) e.title = "Title is required";
    if (tba) {
      const year = parseInt(releaseYear);
      if (!releaseYear || isNaN(year) || year < 2000 || year > 2099)
        e.releaseDate = "Enter a valid year";
    } else {
      if (!releaseDate) e.releaseDate = "Release date is required";
    }
    if (platforms.length === 0) e.platforms = "Select at least one platform";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    const effectiveDate = tba ? `${releaseYear}-12-31` : releaseDate;
    try {
      await onSubmit({
        title: title.trim(),
        description: description.trim() || undefined,
        releaseDate: effectiveDate,
        platforms,
        shopUrl: shopUrl.trim() || undefined,
        imageUrl: imageUrl.trim() || undefined,
        developer: developer.trim() || undefined,
        publisher: publisher.trim() || undefined,
        igdbId,
        tba,
      });
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">

      {/* Title field + RAWG dropdown */}
      <div className="relative" ref={wrapperRef}>
        <div className="flex items-end gap-2">
          <div className="flex-1">
            <Input
              label="Title *"
              value={title}
              onChange={(e) => {
                setTitle(e.target.value);
                if (prefilled) setPrefilled(false);
              }}
              error={errors.title}
              placeholder="e.g. Elden Ring 2"
              autoComplete="off"
            />
          </div>

          {prefillLoading && (
            <div className="mb-2 flex items-center gap-1.5 text-xs text-gray-400 shrink-0">
              <svg className="animate-spin h-3.5 w-3.5" viewBox="0 0 24 24" fill="none">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
              Fetching...
            </div>
          )}
          {prefilled && !prefillLoading && (
            <div className="mb-2 flex items-center gap-1 text-xs font-medium text-emerald-600 shrink-0">
              <svg className="h-3.5 w-3.5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
              Prefilled
            </div>
          )}
        </div>

        {/* Search dropdown */}
        {showDropdown && searchResults.length > 0 && (
          <div className="absolute left-0 right-0 top-full z-50 mt-1 rounded-xl border border-gray-200 bg-white shadow-lg overflow-hidden">
            {searchResults.map((r) => (
              <button
                key={r.igdbId}
                type="button"
                onMouseDown={(e) => e.preventDefault()} // prevent blur before click
                onClick={() => selectResult(r)}
                className="flex items-center gap-3 w-full px-3 py-2.5 text-left hover:bg-gray-50 transition-colors border-b border-gray-100 last:border-b-0"
              >
                <div className="w-10 h-10 rounded-lg overflow-hidden shrink-0 bg-gray-100">
                  {r.imageUrl ? (
                    <img src={r.imageUrl} alt="" className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full bg-gray-200" />
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{r.title}</p>
                  {r.releaseDate && (
                    <p className="text-xs text-gray-400">{r.releaseDate}</p>
                  )}
                </div>
                {r.platforms.length > 0 && (
                  <div className="flex gap-1 shrink-0">
                    {r.platforms.slice(0, 3).map((p) => (
                      <span key={p} className="rounded bg-gray-100 px-1.5 py-0.5 text-xs text-gray-500">
                        {PLATFORM_LABELS[p]}
                      </span>
                    ))}
                  </div>
                )}
              </button>
            ))}
          </div>
        )}
      </div>

      <Input
        label="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Short description"
      />
      <div className="flex flex-col gap-1">
        <div className="flex items-center justify-between">
          <span className="text-sm font-medium text-gray-700">Release date *</span>
          <label className="flex items-center gap-1.5 cursor-pointer select-none">
            <input
              type="checkbox"
              checked={tba}
              onChange={(e) => {
                setTba(e.target.checked);
                setErrors((prev) => ({ ...prev, releaseDate: "" }));
              }}
              className="rounded border-gray-300 text-brand-500 focus:ring-brand-500"
            />
            <span className="text-xs text-gray-500">Date TBA</span>
          </label>
        </div>
        {tba ? (
          <Input
            type="number"
            value={releaseYear}
            onChange={(e) => setReleaseYear(e.target.value)}
            placeholder={String(new Date().getFullYear())}
            error={errors.releaseDate}
            min={2000}
            max={2099}
          />
        ) : (
          <Input
            type="date"
            value={releaseDate}
            onChange={(e) => setReleaseDate(e.target.value)}
            error={errors.releaseDate}
          />
        )}
      </div>

      <div className="flex flex-col gap-1">
        <span className="text-sm font-medium text-gray-700">Platforms *</span>
        <div className="flex gap-2 flex-wrap">
          {PLATFORMS.map((p) => (
            <button
              key={p}
              type="button"
              onClick={() => togglePlatform(p)}
              className={`rounded-lg border px-3 py-1.5 text-sm font-medium transition-colors ${
                platforms.includes(p)
                  ? "border-brand-500 bg-brand-50 text-brand-600"
                  : "border-gray-300 bg-white text-gray-600 hover:border-gray-400"
              }`}
            >
              {PLATFORM_LABELS[p]}
            </button>
          ))}
        </div>
        {errors.platforms && (
          <p className="text-xs text-red-400">{errors.platforms}</p>
        )}
      </div>

      <div className="grid grid-cols-2 gap-3">
        <Input
          label="Developer"
          value={developer}
          onChange={(e) => setDeveloper(e.target.value)}
          placeholder="Studio name"
        />
        <Input
          label="Publisher"
          value={publisher}
          onChange={(e) => setPublisher(e.target.value)}
          placeholder="Publisher name"
        />
      </div>

      <Input
        label="Shop URL"
        type="url"
        value={shopUrl}
        onChange={(e) => setShopUrl(e.target.value)}
        placeholder="https://store.steampowered.com/..."
      />
      <Input
        label="Image URL"
        type="url"
        value={imageUrl}
        onChange={(e) => setImageUrl(e.target.value)}
        placeholder="https://..."
      />

      <div className="flex justify-end gap-2 pt-2">
        <Button type="button" variant="ghost" onClick={onCancel}>
          Cancel
        </Button>
        <Button type="submit" loading={loading}>
          {initial ? "Save changes" : "Add game"}
        </Button>
      </div>
    </form>
  );
}
