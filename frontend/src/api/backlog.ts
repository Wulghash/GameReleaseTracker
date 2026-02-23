import client from "./client";

export type BacklogStatus = "WANT_TO_PLAY" | "PLAYING" | "COMPLETED" | "DROPPED";

export interface BacklogEntry {
  id: string;
  userId: string;
  igdbId: number;
  name: string;
  coverUrl: string | null;
  releaseDate: string | null;
  backlogStatus: BacklogStatus;
  igdbScore: number | null;
  rating: number | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface BacklogAddRequest {
  igdbId: number;
  name: string;
  coverUrl?: string | null;
  releaseDate?: string | null;
  backlogStatus?: BacklogStatus;
  igdbScore?: number | null;
  rating?: number | null;
  notes?: string | null;
}

export interface BacklogUpdateRequest {
  backlogStatus?: BacklogStatus;
  igdbScore?: number | null;
  rating?: number | null;
  notes?: string | null;
}

export const backlogApi = {
  list: (status?: BacklogStatus) =>
    client
      .get<BacklogEntry[]>("/backlog", { params: status ? { status } : undefined })
      .then((r) => r.data),

  add: (data: BacklogAddRequest) =>
    client.post<BacklogEntry>("/backlog", data).then((r) => r.data),

  update: (id: string, data: BacklogUpdateRequest) =>
    client.put<BacklogEntry>(`/backlog/${id}`, data).then((r) => r.data),

  delete: (id: string) => client.delete(`/backlog/${id}`),
};
