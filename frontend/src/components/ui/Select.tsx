import { type SelectHTMLAttributes, forwardRef } from "react";

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
}

export const Select = forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, className = "", children, ...rest }, ref) => (
    <div className="flex flex-col gap-1">
      {label && (
        <label className="text-sm font-medium text-gray-700">{label}</label>
      )}
      <select
        ref={ref}
        className={`
          w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900
          shadow-sm focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20
          disabled:opacity-50 disabled:bg-gray-50
          ${error ? "border-red-400" : ""}
          ${className}
        `}
        {...rest}
      >
        {children}
      </select>
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  )
);
Select.displayName = "Select";
