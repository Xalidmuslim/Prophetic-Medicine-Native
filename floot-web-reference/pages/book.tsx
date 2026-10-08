import { useMemo } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, BookOpen } from "lucide-react";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "../components/Accordion";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import styles from "./book.module.css";

export default function BookPage(){
  const {book,error,loading}=useBook();
  const progress = typeof window === "undefined" ? readerStore.defaults.progress : readerStore.load(window.localStorage).progress;
  const groups = useMemo(() => {
    const map = new Map<string, NonNullable<typeof book>["chapters"]>();
    for(const ch of book?.chapters ?? []) {
      const list = map.get(ch.section) ?? [];
      list.push(ch); map.set(ch.section,list);
    }
    return [...map.entries()];
  },[book]);
  return <>
    <PageHeader title="Читать книгу" subtitle={book ? `${book.chapters.length} глав · полный порядок` : undefined} back/>
    <div className={styles.page}>
      <div className={styles.lead}>
        <BookOpen size={24}/>
        <div><h2>Оригинальное оглавление</h2><p>Все главы идут в порядке полного текста. Тематическая навигация не меняет структуру книги.</p></div>
      </div>
      {loading ? <p className={styles.muted}>Загружаем оглавление…</p> : error ? <p className={styles.error}>{error}</p> : (
        <Accordion type="multiple" defaultValue={groups.slice(0,2).map(([name])=>name)} className={styles.accordion}>
          {groups.map(([section,chapters])=>(
            <AccordionItem key={section} value={section} className={styles.group}>
              <AccordionTrigger className={styles.trigger}>
                <span><strong>{section}</strong><small>{chapters.length} глав</small></span>
              </AccordionTrigger>
              <AccordionContent>
                <div className={styles.rows}>
                  {chapters.map((ch)=>{
                    const item = progress.items[ch.id];
                    const pct = Math.round((item?.ratio ?? 0)*100);
                    return <Link key={ch.id} to={`/read/${ch.slug}`} className={styles.row}>
                      <span className={styles.order}>{String(ch.order).padStart(2,"0")}</span>
                      <span className={styles.rowText}><strong>{ch.title}</strong>{pct ? <span className={styles.line}><i style={{width:`${pct}%`}}/></span> : null}</span>
                      {pct ? <span className={styles.pct}>{pct}%</span> : <ArrowRight size={16}/>}
                    </Link>;
                  })}
                </div>
              </AccordionContent>
            </AccordionItem>
          ))}
        </Accordion>
      )}
    </div>
  </>;
}