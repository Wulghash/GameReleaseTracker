import { Link } from "react-router-dom";
import { format, differenceInDays, differenceInCalendarMonths } from "date-fns";
import type { Game } from "../api/games";

const PLATFORM_LABELS: Record<string, string> = {
  PC: "PC", PS5: "PS5", XBOX: "Xbox", SWITCH: "Switch",
};

const PLACEHOLDER_GRADIENTS = [
  "from-blue-900 to-slate-950",
  "from-violet-900 to-slate-950",
  "from-rose-900 to-gray-950",
  "from-emerald-900 to-gray-950",
  "from-amber-900 to-gray-950",
  "from-cyan-900 to-gray-950",
  "from-indigo-900 to-slate-950",
  "from-fuchsia-900 to-gray-950",
];

function placeholderGradient(title: string): string {
  let hash = 0;
  for (const c of title) hash = (hash * 31 + c.charCodeAt(0)) >>> 0;
  return PLACEHOLDER_GRADIENTS[hash % PLACEHOLDER_GRADIENTS.length];
}

function daysUntilRelease(releaseDate: string): number {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return differenceInDays(new Date(releaseDate), today);
}

function countdownLabel(releaseDate: string): string {
  const days = daysUntilRelease(releaseDate);
  if (days < 0)    return "";
  if (days === 0)  return "today";
  if (days === 1)  return "tomorrow";
  if (days < 14)   return `in ${days} days`;
  if (days < 60)   return `in ${Math.round(days / 7)} weeks`;
  const months = differenceInCalendarMonths(new Date(releaseDate), new Date());
  if (months < 12) return `in ${months} month${months !== 1 ? "s" : ""}`;
  const years = Math.round(months / 12);
  return `in ${years} year${years !== 1 ? "s" : ""}`;
}

export function GameRow({ game }: { game: Game }) {
  const gradient = placeholderGradient(game.title);
  const days = game.status === "UPCOMING" && !game.tba ? daysUntilRelease(game.releaseDate) : null;
  const cd = game.status === "UPCOMING" && !game.tba ? countdownLabel(game.releaseDate) : "";

  return (
    <Link
      to={`/games/${game.id}`}
      className="group flex items-start gap-4 px-5 py-4 hover:bg-gray-50 transition-colors"
    >
      {/* Thumbnail */}
      <div className="w-28 h-28 shrink-0 rounded-xl overflow-hidden">
        {game.imageUrl ? (
          <img
            src={game.imageUrl}
            alt={game.title}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className={`w-full h-full bg-gradient-to-br ${gradient} flex items-end p-2.5`}>
            <span className="text-sm font-bold text-white/20 leading-tight line-clamp-3">
              {game.title}
            </span>
          </div>
        )}
      </div>

      {/* Info */}
      <div className="flex-1 min-w-0 flex flex-col gap-2 pt-0.5">
        <h3 className="text-base font-semibold text-gray-900 group-hover:text-brand-600 transition-colors line-clamp-2 leading-snug">
          {game.title}
        </h3>

        {/* Pills row */}
        <div className="flex flex-wrap items-center gap-1.5">
          {game.platforms.map((p) => (
            <span
              key={p}
              className="inline-flex items-center rounded-full bg-indigo-50 px-2 py-0.5 text-xs font-medium text-indigo-700 ring-1 ring-inset ring-indigo-200"
            >
              {PLATFORM_LABELS[p]}
            </span>
          ))}

          {/* Urgency badge */}
          {days !== null && days >= 0 && days <= 7 && (
            <span className="inline-flex items-center rounded-full bg-orange-50 px-2 py-0.5 text-xs font-semibold text-orange-700 ring-1 ring-inset ring-orange-200">
              {days === 0 ? "Out now!" : "This week"}
            </span>
          )}
        </div>

        {/* Studio */}
        {(game.developer || game.publisher) && (
          <p className="text-xs text-gray-400 truncate">
            {[game.developer, game.publisher].filter(Boolean).join(" · ")}
          </p>
        )}
      </div>

      {/* Date + countdown */}
      <div className="flex flex-col items-end gap-1 shrink-0 pt-0.5">
        {game.tba ? (
          <>
            <span className="text-lg font-bold text-gray-900 tabular-nums leading-tight">
              {game.releaseDate.slice(0, 4)}
            </span>
            <span className="inline-flex items-center rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-500">
              TBA
            </span>
          </>
        ) : (
          <>
            <span className="text-lg font-bold text-gray-900 tabular-nums leading-tight">
              {format(new Date(game.releaseDate), "MMM d")}
            </span>
            <span className="text-xs text-gray-400 tabular-nums">
              {format(new Date(game.releaseDate), "yyyy")}
            </span>
            {cd && (
              <span className="text-xs font-semibold text-amber-600 mt-1">{cd}</span>
            )}
          </>
        )}
        {game.status === "RELEASED" && (
          <span className="text-xs font-semibold text-green-600 mt-1">Released</span>
        )}
        {game.status === "CANCELLED" && (
          <span className="text-xs font-semibold text-red-500 mt-1">Cancelled</span>
        )}
      </div>
    </Link>
  );
}
