import { ExternalLink } from "lucide-react";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import styles from "./source.module.css";

const SOURCE_URL="/_cdn/static/82da772c-ee48-4088-b869-a69fb41348aa-tibb_full_ru_work.md";
export default function SourcePage(){
  const {book,error,loading}=useBook();
  return <><PageHeader title="Об источнике текста" back/><div className={styles.page}>
    <h2>Полный русский текст</h2>
    <p>Приложение загружает один канонический Markdown-файл и строит из него главы, темы, средства и поиск. Текст не хранится в нескольких расходящихся копиях.</p>
    {loading?<p>Считаем структуру…</p>:error?<p className={styles.error}>{error}</p>:book?<div className={styles.stats}><div><strong>{book.stats.chapters}</strong><span>глав</span></div><div><strong>{book.stats.blocks}</strong><span>блоков</span></div><div><strong>{book.stats.sourceWords.toLocaleString("ru-RU")}</strong><span>слов</span></div><div><strong>{book.stats.remedies}</strong><span>средств</span></div></div>:null}
    <section><h3>Редакционный принцип</h3><p>Лечебные описания автора сохранены полностью. Нераспознанный структурным парсером текст остаётся обычным текстом и не отбрасывается.</p></section>
    <a className={styles.sourceLink} href={SOURCE_URL} target="_blank" rel="noreferrer">Открыть исходный текст <ExternalLink size={15}/></a>
  </div></>;
}