import { parseBook } from "./parseBook";
import { BookModel } from "./BookModel";

const SOURCE_URL = "/_cdn/static/82da772c-ee48-4088-b869-a69fb41348aa-tibb_full_ru_work.md";
const SOURCE_CACHE_KEY = "mp:v1:book-source";
let cache: Promise<BookModel.Book> | null = null;

const validateSource = (text: string) => {
  const words = text.trim().split(/\s+/).length;
  if (words < 75000 || !text.includes("# Заключительная глава:") || !text.includes("# МЕДИЦИНА ПРОРОКА ﷺ")) {
    throw new Error("Текст книги загружен не полностью");
  }
  return text;
};

export function loadBook(): Promise<BookModel.Book> {
  if (!cache) {
    cache = fetch(SOURCE_URL)
      .then(async (response) => {
        if (!response.ok) throw new Error("Не удалось загрузить текст книги");
        const text = validateSource(await response.text());
        try { window.localStorage.setItem(SOURCE_CACHE_KEY, text); } catch { /* cache is optional */ }
        return text;
      })
      .catch((error) => {
        try {
          const stored = window.localStorage.getItem(SOURCE_CACHE_KEY);
          if (stored) return validateSource(stored);
        } catch { /* storage can be unavailable */ }
        throw error;
      })
      .then(parseBook);
  }
  return cache;
}