import { Link } from "react-router-dom";
import { differenceInDays, differenceInCalendarMonths } from "date-fns";
import type { Game } from "../api/games";

const PLATFORM_LABELS: Record<string, string> = {
  PC: "PC", PS5: "PS5", XBOX: "Xbox", SWITCH: "Switch",
};

const PLACEHOLDER_GRADIENTS = [
  "from-blue-900 to-slate-950",
  "from-purple-900 to-slate-950",
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

function countdown(releaseDate: string): string {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const release = new Date(releaseDate);
  const days = differenceInDays(release, today);

  if (days < 0)  return "out now";
  if (days === 0) return "today";
  if (days === 1) return "tomorrow";
  if (days < 14)  return `in ${days} days`;
  if (days < 60)  return `in ${Math.round(days / 7)}w`;
  const months = differenceInCalendarMonths(release, today);
  if (months < 12) return `in ${months}mo`;
  const years = Math.round(months / 12);
  return `in ${years}y`;
}

export function GameCard({ game }: { game: Game }) {
  const gradient = placeholderGradient(game.title);
  const platforms = game.platforms.map((p) => PLATFORM_LABELS[p]).join(" · ");

  return (
    <Link
      to={`/games/${game.id}`}
      className="group flex flex-col rounded-xl overflow-hidden border border-gray-800/60 hover:border-gray-600 transition-all hover:-translate-y-0.5"
    >
      {/* Portrait image area */}
      <div className="relative aspect-[2/3] flex-shrink-0">
        {game.imageUrl ? (
          <>
            <img
              src={game.imageUrl}
              alt={game.title}
              className="absolute inset-0 h-full w-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-gray-950/90 via-transparent to-transparent" />
          </>
        ) : (
          <div className={`absolute inset-0 bg-gradient-to-br ${gradient}`}>
            <div className="absolute inset-0 flex items-end p-3">
              <span className="text-xl font-bold text-white/20 leading-tight line-clamp-4">
                {game.title}
              </span>
            </div>
          </div>
        )}

        {/* Countdown pill — top right */}
        {game.status === "UPCOMING" && (
          <span className="absolute top-2 right-2 rounded-full bg-black/60 backdrop-blur-sm px-2 py-0.5 text-[10px] font-semibold text-yellow-300 tabular-nums">
            {countdown(game.releaseDate)}
          </span>
        )}

        {game.status === "RELEASED" && (
          <span className="absolute top-2 right-2 rounded-full bg-black/60 backdrop-blur-sm px-2 py-0.5 text-[10px] font-semibold text-green-400">
            Released
          </span>
        )}

        {game.status === "CANCELLED" && (
          <span className="absolute top-2 right-2 rounded-full bg-black/60 backdrop-blur-sm px-2 py-0.5 text-[10px] font-semibold text-red-400">
            Cancelled
          </span>
        )}
      </div>

      {/* Info strip */}
      <div className="flex flex-col gap-1 bg-gray-900 px-3 py-2.5">
        <p className="text-sm font-semibold text-gray-100 group-hover:text-white transition-colors leading-tight line-clamp-2">
          {game.title}
        </p>
        <p className="text-[11px] text-gray-500">{platforms}</p>
      </div>
    </Link>
  );
}
