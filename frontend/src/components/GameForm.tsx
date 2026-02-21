import { useState, type FormEvent } from "react";
import type { Game, GameFormData, Platform } from "../api/games";
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
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(false);

  function togglePlatform(p: Platform) {
    setPlatforms((prev) =>
      prev.includes(p) ? prev.filter((x) => x !== p) : [...prev, p]
    );
  }

  function validate(): boolean {
    const e: Record<string, string> = {};
    if (!title.trim()) e.title = "Title is required";
    if (!releaseDate) e.releaseDate = "Release date is required";
    if (platforms.length === 0) e.platforms = "Select at least one platform";
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      await onSubmit({
        title: title.trim(),
        description: description.trim() || undefined,
        releaseDate,
        platforms,
        shopUrl: shopUrl.trim() || undefined,
        imageUrl: imageUrl.trim() || undefined,
        developer: developer.trim() || undefined,
        publisher: publisher.trim() || undefined,
      });
    } finally {
      setLoading(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <Input
        label="Title *"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        error={errors.title}
        placeholder="e.g. Elden Ring 2"
      />
      <Input
        label="Description"
        value={description}
        onChange={(e) => setDescription(e.target.value)}
        placeholder="Short description"
      />
      <Input
        label="Release date *"
        type="date"
        value={releaseDate}
        onChange={(e) => setReleaseDate(e.target.value)}
        error={errors.releaseDate}
      />

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
