import { type ReactNode } from "react";
import { Link } from "react-router-dom";
import { Gamepad2 } from "lucide-react";

export function Layout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <header className="bg-white border-b border-gray-200 sticky top-0 z-40">
        <div className="max-w-2xl mx-auto px-4 h-14 flex items-center">
          <Link
            to="/"
            className="flex items-center gap-2.5 text-gray-700 hover:text-gray-900 transition-colors"
          >
            <Gamepad2 size={18} className="text-brand-500" />
            <span className="text-sm font-semibold tracking-tight">
              Game Release Tracker
            </span>
          </Link>
        </div>
      </header>

      <main className="max-w-2xl mx-auto w-full px-4 py-10">
        {children}
      </main>
    </div>
  );
}
