import { useAppSelector, useAppDispatch } from "@/hooks/redux";
import { setTheme } from "@/store/slices/uiSlice";

export function useTheme() {
  const dispatch = useAppDispatch();
  const theme = useAppSelector((state) => state.ui.theme);

  const toggleTheme = () => {
    dispatch(setTheme(theme === "dark" ? "light" : "dark"));
  };

  return {
    theme,
    toggleTheme,
    setTheme: (newTheme: "light" | "dark") => dispatch(setTheme(newTheme)),
  };
}
