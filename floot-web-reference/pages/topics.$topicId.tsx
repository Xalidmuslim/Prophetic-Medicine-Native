import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowRight, Star } from "lucide-react";
import { Button } from "../components/Button";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import styles from "./topics.$topicId.module.css";

export default function TopicPage(){
  const {topicId}=useParams(); const {book,error,loading}=useBook();
  const [favorite,setFavorite]=useState(()=>typeof window!=="undefined"&&readerStore.load(window.localStorage).favoriteTopics.items.includes(topicId??""));
  const topic=book?.topics.find((t)=>t.id===topicId);
  const chapters=topic?topic.chapterIds.map((id)=>book!.chapters.find((ch)=>ch.id===id)).filter(Boolean):[];
  const toggle=()=>{if(!topic)return;const items=readerStore.toggleFavoriteTopic(window.localStorage,topic.id);setFavorite(items.includes(topic.id));};
  return <>
    <PageHeader title={topic?.title??"Тема"} back action={topic?<Button variant={favorite?"secondary":"ghost"} size="icon-md" aria-label="Избранная тема" onClick={toggle}><Star size={18} fill={favorite?"currentColor":"none"}/></Button>:undefined}/>
    <div className={styles.page}>
      {loading?<p>Загрузка…</p>:error||!topic?<p className={styles.error}>{error??"Тема не найдена"}</p>:<>
        <div className={styles.lead}><span>{chapters.length} связанных глав</span><h2>{topic.title}</h2><p>Переход открывает исходную главу книги. Текст внутри темы не пересказывается и не сокращается.</p></div>
        <div className={styles.list}>{chapters.map((ch)=><Link key={ch!.id} to={`/read/${ch!.slug}`} className={styles.row}><span className={styles.order}>{String(ch!.order).padStart(2,"0")}</span><span><strong>{ch!.title}</strong><small>{ch!.section}</small></span><ArrowRight size={16}/></Link>)}</div>
      </>}
    </div>
  </>;
}