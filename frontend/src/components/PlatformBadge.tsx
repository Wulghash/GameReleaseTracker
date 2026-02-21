import { Badge } from "./ui/Badge";
import type { Platform } from "../api/games";

const label: Record<Platform, string> = {
  PC:     "PC",
  PS5:    "PS5",
  XBOX:   "Xbox",
  SWITCH: "Switch",
};

export function PlatformBadge({ platform }: { platform: Platform }) {
  return <Badge variant="muted">{label[platform]}</Badge>;
}
