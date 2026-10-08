import { BookModel } from "./BookModel";

const normalize = (value: string) =>
  String(value ?? "")
    .toLocaleLowerCase("ru")
    .replaceAll("ё", "е")
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zа-я0-9\s-]+/gi, " ")
    .replace(/\s+/g, " ")
    .trim();

const plain = (value: string) =>
  String(value ?? "")
    .replace(/^\s*>\s?/gm, "")
    .replace(/\*\*|__|`/g, "")
    .replace(/\*([^*]+)\*/g, "$1")
    .replace(/\s+/g, " ")
    .trim();

const snippet = (text: string, terms: string[], max = 220) => {
  const clean = plain(text);
  const normalized = normalize(clean);
  let position = -1;
  for (const term of terms) {
    const p = normalized.indexOf(term);
    if (p >= 0 && (position < 0 || p < position)) position = p;
  }
  if (position < 0) return clean.slice(0,max) + (clean.length > max ? "…" : "");
  const start = Math.max(0, position - 70);
  const end = Math.min(clean.length, start + max);
  return `${start ? "…" : ""}${clean.slice(start,end).trim()}${end < clean.length ? "…" : ""}`;
};

export function searchBook(book: BookModel.Book, query: string, limit = 80): BookModel.SearchResult[] {
  const q = normalize(query);
  if (!q) return [];
  const terms = new Set<string>([q]);
  for (const remedy of book.remedies) {
    const names = [remedy.title, ...remedy.aliases].map(normalize).filter(Boolean);
    if (names.some((name) => name.includes(q) || q.includes(name))) names.forEach((name) => terms.add(name));
  }
  const expanded = [...terms];
  const results: BookModel.SearchResult[] = [];
  for (const chapter of book.chapters) {
    let titleMatch = expanded.some((term) => normalize(chapter.title).includes(term));
    for (const block of chapter.contentBlocks) {
      if (titleMatch || expanded.some((term) => normalize(block.text).includes(term))) {
        results.push({
          chapterId:chapter.id,
          chapterSlug:chapter.slug,
          chapterTitle:chapter.title,
          anchor:block.anchor,
          type:block.type,
          snippet:snippet(block.text,expanded),
        });
        titleMatch = false;
        if (results.length >= limit) return results;
      }
    }
  }
  return results;
}