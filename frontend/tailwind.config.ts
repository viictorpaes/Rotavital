import type { Config } from "tailwindcss";

export default
{
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme:
  {
    extend:
    {
      colors:
      {
        rota:
        {
          bg: "#f4f3f0",
          surface: "#ffffff",
          surface2: "#efeee9",
          border: "#e2e0d8",
          red: "#c1272d",
          redDark: "#961e23",
        },
      },
      fontFamily:
      {
        sans: [
          "Inter",
          "ui-sans-serif",
          "system-ui",
          "-apple-system",
          "Segoe UI",
          "Roboto",
          "sans-serif",
        ],
      },
      boxShadow:
      {
        card: "0 1px 2px 0 rgb(0 0 0 / 0.3), 0 1px 3px 1px rgb(0 0 0 / 0.2)",
      },
    },
  },
  plugins: [],
} satisfies Config;
