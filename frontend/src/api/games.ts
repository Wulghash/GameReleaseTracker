import client from "./client";

export type Platform = "PC" | "PS5" | "XBOX" | "SWITCH";
export type GameStatus = "UPCOMING" | "RELEASED" | "CANCELLED";

export interface Game {
  id: string;
  title: string;
  description: string | null;
  releaseDate: string;
  platforms: Platform[];
  status: GameStatus;
  shopUrl: string | null;
  imageUrl: string | null;
  developer: string | null;
  publisher: string | null;
  igdbId: number | null;
  tba: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface GamePage {
  content: Game[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface GameFilters {
  platform?: Platform;
  status?: GameStatus;
  releaseDateFrom?: string;
  releaseDateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export interface GameFormData {
  title: string;
  description?: string;
  releaseDate: string;
  platforms: Platform[];
  shopUrl?: string;
  imageUrl?: string;
  developer?: string;
  publisher?: string;
  igdbId?: number;
  tba?: boolean;
}

export interface IgdbSearchResult {
  igdbId: number;
  title: string;
  releaseDate: string | null;
  imageUrl: string | null;
  platforms: Platform[];
}

export interface IgdbGameDetail {
  title: string;
  releaseDate: string | null;
  imageUrl: string | null;
  platforms: Platform[];
  description: string | null;
  developer: string | null;
  publisher: string | null;
}

export const gamesApi = {
  list: (filters: GameFilters = {}) =>
    client.get<GamePage>("/games", { params: filters }).then((r) => r.data),

  getById: (id: string) =>
    client.get<Game>(`/games/${id}`).then((r) => r.data),

  create: (data: GameFormData) =>
    client.post<Game>("/games", data).then((r) => r.data),

  update: (id: string, data: GameFormData) =>
    client.put<Game>(`/games/${id}`, data).then((r) => r.data),

  updateStatus: (id: string, status: GameStatus) =>
    client.patch<Game>(`/games/${id}/status`, { status }).then((r) => r.data),

  delete: (id: string) => client.delete(`/games/${id}`),

  subscribe: (id: string, email: string) =>
    client.post(`/games/${id}/subscribe`, { email }),

  lookupSearch: (q: string) =>
    client.get<IgdbSearchResult[]>("/games/lookup", { params: { q } }).then((r) => r.data),

  lookupDetail: (igdbId: number) =>
    client.get<IgdbGameDetail>(`/games/lookup/${igdbId}`).then((r) => r.data),
};
