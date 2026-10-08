import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Search } from "lucide-react";
import { Input } from "../components/Input";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import styles from "./remedies.module.css";

const normalize=(v:string)=>v.toLocaleLowerCase("ru").replaceAll("ё","е");

export default function RemediesPage(){
  const {book,error,loading}=useBook(); const [query,setQuery]=useState("");
  const remedies=useMemo(()=>book?.remedies.filter((r)=>!query.trim()||[r.title,...r.aliases].some((name)=>normalize(name).includes(normalize(query.trim()))))??[],[book,query]);
  return <>
    <PageHeader title="Средства" subtitle={book ? String(book.remedies.length)+" позиций из книги" : undefined} back/>
    <div className={styles.page}>
      <div className={styles.note}>Справочник служит только навигацией по тексту Ибн аль-Каййима. Он не создаёт новых лечебных рекомендаций.</div>
      <div className={styles.search}><Search size={18}/><Input value={query} onChange={(e)=>setQuery(e.target.value)} placeholder="Тмин, кыст, мёд, сивак…"/></div>
      {loading?<p>Загрузка…</p>:error?<p className={styles.error}>{error}</p>:<div className={styles.list}>{remedies.map((r)=><Link key={r.id} to={"/remedies/"+r.id} className={styles.row}><span><strong>{r.title}</strong><small>{r.chapterIds.length} упоминаний{r.aliases.length ? " · "+r.aliases.slice(0,2).join(", ") : ""}</small></span><ArrowRight size={17}/></Link>)}</div>}
      {!loading&&!error&&!remedies.length?<p className={styles.empty}>Ничего не найдено. Попробуйте другое написание.</p>:null}
    </div>
  </>;
}