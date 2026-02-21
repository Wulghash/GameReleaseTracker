import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { format } from "date-fns";
import {
  ArrowLeft, Calendar, Pencil, Trash2, Bell, ExternalLink, RefreshCw,
} from "lucide-react";
import { gamesApi, type GameFormData, type GameStatus } from "../api/games";
import { StatusBadge } from "../components/StatusBadge";
import { PlatformBadge } from "../components/PlatformBadge";
import { GameForm } from "../components/GameForm";
import { SubscribeModal } from "../components/SubscribeModal";
import { Modal } from "../components/ui/Modal";
import { Button } from "../components/ui/Button";
import { Select } from "../components/ui/Select";

export function GameDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [editOpen, setEditOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [subscribeOpen, setSubscribeOpen] = useState(false);
  const [statusValue, setStatusValue] = useState<GameStatus | "">("");

  const { data: game, isLoading, isError } = useQuery({
    queryKey: ["game", id],
    queryFn: () => gamesApi.getById(id!),
    enabled: !!id,
  });

  const updateGame = useMutation({
    mutationFn: (data: GameFormData) => gamesApi.update(id!, data),
    onSuccess: (updated) => {
      queryClient.setQueryData(["game", id], updated);
      queryClient.invalidateQueries({ queryKey: ["games"] });
      setEditOpen(false);
    },
  });

  const updateStatus = useMutation({
    mutationFn: (status: GameStatus) => gamesApi.updateStatus(id!, status),
    onSuccess: (updated) => {
      queryClient.setQueryData(["game", id], updated);
      queryClient.invalidateQueries({ queryKey: ["games"] });
      setStatusValue("");
    },
  });

  const deleteGame = useMutation({
    mutationFn: () => gamesApi.delete(id!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["games"] });
      navigate("/");
    },
  });

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="h-5 w-16 rounded bg-gray-200 animate-pulse" />
        <div className="h-52 rounded-xl bg-gray-200 animate-pulse" />
        <div className="h-8 w-64 rounded bg-gray-200 animate-pulse" />
      </div>
    );
  }

  if (isError || !game) {
    return <p className="text-red-500">Game not found.</p>;
  }

  const allowedTransitions: Partial<Record<GameStatus, GameStatus[]>> = {
    UPCOMING: ["RELEASED", "CANCELLED"],
  };
  const transitions = allowedTransitions[game.status] ?? [];

  return (
    <div className="flex flex-col gap-6">
      {/* Back */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-800 transition-colors w-fit"
      >
        <ArrowLeft size={15} />
        Back
      </button>

      {/* Cover */}
      {game.imageUrl ? (
        <img
          src={game.imageUrl}
          alt={game.title}
          className="w-full rounded-xl object-cover max-h-72 border border-gray-200"
        />
      ) : (
        <div className="w-full rounded-xl bg-gray-100 border border-gray-200 flex items-center justify-center h-36">
          <span className="text-5xl">🎮</span>
        </div>
      )}

      {/* Title + status */}
      <div className="flex items-start justify-between gap-4">
        <h1 className="text-2xl font-bold text-gray-900">{game.title}</h1>
        <StatusBadge status={game.status} />
      </div>

      {/* Meta */}
      <div className="flex flex-col gap-2 text-sm text-gray-600">
        <span className="flex items-center gap-2">
          <Calendar size={14} className="text-gray-400" />
          {format(new Date(game.releaseDate), "MMMM d, yyyy")}
        </span>
        <div className="flex flex-wrap gap-1.5">
          {game.platforms.map((p) => <PlatformBadge key={p} platform={p} />)}
        </div>
        {game.developer && (
          <span>Developer: <strong className="text-gray-800 font-medium">{game.developer}</strong></span>
        )}
        {game.publisher && (
          <span>Publisher: <strong className="text-gray-800 font-medium">{game.publisher}</strong></span>
        )}
        {game.shopUrl && (
          <a
            href={game.shopUrl}
            target="_blank"
            rel="noreferrer"
            className="flex items-center gap-1 text-brand-500 hover:text-brand-600 transition-colors w-fit"
          >
            <ExternalLink size={13} />
            Shop page
          </a>
        )}
      </div>

      {game.description && (
        <p className="text-gray-600 leading-relaxed text-sm">{game.description}</p>
      )}

      {/* Actions */}
      <div className="flex flex-wrap gap-2 border-t border-gray-200 pt-5">
        {game.status === "UPCOMING" && (
          <Button onClick={() => setSubscribeOpen(true)}>
            <Bell size={14} />
            Subscribe
          </Button>
        )}

        <Button variant="secondary" onClick={() => setEditOpen(true)}>
          <Pencil size={14} />
          Edit
        </Button>

        {transitions.length > 0 && (
          <div className="flex items-center gap-2">
            <Select
              className="w-40"
              value={statusValue}
              onChange={(e) => setStatusValue(e.target.value as GameStatus)}
            >
              <option value="">Change status…</option>
              {transitions.map((s) => (
                <option key={s} value={s}>
                  {s.charAt(0) + s.slice(1).toLowerCase()}
                </option>
              ))}
            </Select>
            {statusValue && (
              <Button
                size="sm"
                variant="secondary"
                loading={updateStatus.isPending}
                onClick={() => updateStatus.mutate(statusValue as GameStatus)}
              >
                <RefreshCw size={13} />
                Apply
              </Button>
            )}
          </div>
        )}

        <Button
          variant="danger"
          className="ml-auto"
          onClick={() => setDeleteOpen(true)}
        >
          <Trash2 size={14} />
          Delete
        </Button>
      </div>

      {/* Edit modal */}
      <Modal open={editOpen} onClose={() => setEditOpen(false)} title="Edit game">
        <GameForm
          initial={game}
          onSubmit={(data) => updateGame.mutateAsync(data).then(() => {})}
          onCancel={() => setEditOpen(false)}
        />
      </Modal>

      {/* Delete confirm */}
      <Modal open={deleteOpen} onClose={() => setDeleteOpen(false)} title="Delete game">
        <div className="flex flex-col gap-4">
          <p className="text-sm text-gray-600">
            Are you sure you want to delete <strong className="text-gray-900">{game.title}</strong>? This cannot be undone.
          </p>
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setDeleteOpen(false)}>Cancel</Button>
            <Button
              variant="danger"
              loading={deleteGame.isPending}
              onClick={() => deleteGame.mutate()}
            >
              Delete
            </Button>
          </div>
        </div>
      </Modal>

      {/* Subscribe modal */}
      <SubscribeModal
        gameId={game.id}
        gameTitle={game.title}
        open={subscribeOpen}
        onClose={() => setSubscribeOpen(false)}
      />
    </div>
  );
}
