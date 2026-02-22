import { Gamepad2 } from "lucide-react";

export function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="bg-white rounded-xl border border-gray-200 shadow-sm p-10 flex flex-col items-center gap-6 w-full max-w-sm">
        <div className="flex items-center gap-2.5 text-gray-700">
          <Gamepad2 size={22} className="text-brand-500" />
          <span className="text-base font-semibold tracking-tight">
            Game Release Tracker
          </span>
        </div>

        <p className="text-sm text-gray-500 text-center">
          Track upcoming game releases and get notified when they drop.
        </p>

        <a
          href="/oauth2/authorization/google"
          className="w-full flex items-center justify-center gap-3 px-4 py-2.5 rounded-lg border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
        >
          <img
            src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg"
            alt="Google"
            className="w-5 h-5"
          />
          Sign in with Google
        </a>
      </div>
    </div>
  );
}
