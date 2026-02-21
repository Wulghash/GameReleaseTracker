import { Badge } from "./ui/Badge";
import type { GameStatus } from "../api/games";

const statusConfig: Record<GameStatus, { label: string; variant: "success" | "warning" | "danger" }> = {
  UPCOMING:  { label: "Upcoming",  variant: "warning" },
  RELEASED:  { label: "Released",  variant: "success" },
  CANCELLED: { label: "Cancelled", variant: "danger"  },
};

export function StatusBadge({ status }: { status: GameStatus }) {
  const { label, variant } = statusConfig[status];
  return <Badge variant={variant}>{label}</Badge>;
}
