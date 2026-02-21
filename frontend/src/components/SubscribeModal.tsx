import { useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import { gamesApi } from "../api/games";
import { Modal } from "./ui/Modal";
import { Input } from "./ui/Input";
import { Button } from "./ui/Button";

interface SubscribeModalProps {
  gameId: string;
  gameTitle: string;
  open: boolean;
  onClose: () => void;
}

export function SubscribeModal({ gameId, gameTitle, open, onClose }: SubscribeModalProps) {
  const [email, setEmail] = useState("");
  const [done, setDone] = useState(false);

  const subscribe = useMutation({
    mutationFn: () => gamesApi.subscribe(gameId, email),
    onSuccess: () => setDone(true),
  });

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (email.trim()) subscribe.mutate();
  }

  function handleClose() {
    setEmail("");
    setDone(false);
    onClose();
  }

  return (
    <Modal open={open} onClose={handleClose} title={`Subscribe to ${gameTitle}`}>
      {done ? (
        <div className="flex flex-col items-center gap-4 py-4 text-center">
          <span className="text-4xl">🎉</span>
          <p className="text-gray-600">
            You're subscribed! We'll email you 7 days before and on release day.
          </p>
          <Button onClick={handleClose}>Close</Button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <p className="text-sm text-gray-600">
            Enter your email to get notified about <strong className="text-gray-200">{gameTitle}</strong>.
          </p>
          <Input
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="you@example.com"
            required
            error={subscribe.isError ? "Subscription failed. You may already be subscribed." : undefined}
          />
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={handleClose}>
              Cancel
            </Button>
            <Button type="submit" loading={subscribe.isPending}>
              Subscribe
            </Button>
          </div>
        </form>
      )}
    </Modal>
  );
}
