import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Search as SearchIcon } from "lucide-react";
import { Input } from "../components/Input";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { searchBook } from "../helpers/searchBook";
import styles from "./search.module.css";

export default function SearchPage(){
  const {book,error,loading}=useBook(); const [query,setQuery]=useState("");
  const results=useMemo(()=>book&&query.trim().length>=2?searchBook(book,query.trim()):[],[book,query]);
  return <>
    <PageHeader title="Поиск" subtitle="По всему полному тексту"/>
    <div className={styles.page}>
      <div className={styles.searchbox}><SearchIcon size={20}/><Input autoFocus value={query} onChange={(e)=>setQuery(e.target.value)} placeholder="Введите слово или фразу"/></div>
      <p className={styles.hint}>Учитываются варианты <strong>е/ё</strong> и алиасы основных средств.</p>
      {loading?<p>Загружаем индекс…</p>:error?<p className={styles.error}>{error}</p>:query.trim().length<2?<div className={styles.empty}>Начните вводить запрос. Например: «хиджама», «Замзам», «головная боль».</div>:results.length?<div className={styles.results}><div className={styles.count}>Найдено: {results.length}</div>{results.map((r,i)=><Link key={r.chapterId+"-"+r.anchor+"-"+i} to={"/read/"+r.chapterSlug+"#"+r.anchor} className={styles.result}><span className={styles.type}>{r.type==="quran"?"Коран":r.type==="hadith"?"Хадис":r.type==="historical_note"?"Медицина эпохи":"Текст"}</span><strong>{r.chapterTitle}</strong><p>{r.snippet}</p></Link>)}</div>:<div className={styles.empty}>Ничего не найдено. Проверьте написание или попробуйте более короткий запрос.</div>}
    </div>
  </>;
}