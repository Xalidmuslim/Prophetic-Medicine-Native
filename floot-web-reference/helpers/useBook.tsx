import { useEffect, useState } from "react";
import { loadBook } from "./loadBook";
import { BookModel } from "./BookModel";

export function useBook() {
  const [book,setBook] = useState<BookModel.Book | null>(null);
  const [error,setError] = useState<string | null>(null);
  useEffect(() => {
    let active = true;
    loadBook().then((value) => { if (active) setBook(value); }).catch((reason) => {
      if (active) setError(reason instanceof Error ? reason.message : "Не удалось загрузить книгу");
    });
    return () => { active = false; };
  },[]);
  return {book,error,loading:!book && !error};
}