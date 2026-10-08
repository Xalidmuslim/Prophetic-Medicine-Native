import { useState } from "react";
import { Link } from "react-router-dom";
import { Clock3 } from "lucide-react";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import styles from "./history.module.css";

export default function HistoryPage(){
  const {book,error,loading}=useBook();
  const [history]=useState(()=>typeof window==="undefined"?readerStore.defaults.history:readerStore.load(window.localStorage).history);
  return <>
    <PageHeader title="История чтения" back/>
    <div className={styles.page}>
      {loading?<p>Загрузка…</p>:error?<p className={styles.error}>{error}</p>:history.items.length?<div className={styles.list}>{history.items.map((item)=>{
        const chapter=book!.chapters.find((ch)=>ch.id===item.chapterId); if(!chapter)return null;
        return <Link key={item.chapterId} to={"/read/"+chapter.slug+(item.anchor?"#"+item.anchor:"")} className={styles.row}><Clock3 size={16}/><span><strong>{chapter.title}</strong><small>{new Date(item.visitedAt).toLocaleString("ru-RU",{day:"numeric",month:"short",hour:"2-digit",minute:"2-digit"})}</small></span></Link>;
      })}</div>:<div className={styles.empty}><Clock3 size={30}/><h2>История пуста</h2><p>Последние открытые главы появятся здесь автоматически.</p></div>}
    </div>
  </>;
}