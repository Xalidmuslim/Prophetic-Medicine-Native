import { Link } from "react-router-dom";
import { ArrowRight, Star } from "lucide-react";
import { Button } from "../components/Button";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { readerStore } from "../helpers/readerStore";
import styles from "./topics.module.css";

export default function TopicsPage(){
  const {book,error,loading}=useBook();
  const favorites=typeof window==="undefined"?[]:readerStore.load(window.localStorage).favoriteTopics.items;
  return <>
    <PageHeader title="Темы" subtitle="Изучение поверх полного текста"/>
    <div className={styles.page}>
      <div className={styles.lead}><h2>По смыслу, не только по порядку</h2><p>Одна глава может входить сразу в несколько тем. Полное оригинальное оглавление при этом остаётся неизменным.</p></div>
      {loading?<p>Загружаем темы…</p>:error?<p className={styles.error}>{error}</p>:
        <div className={styles.grid}>{book!.topics.map((topic)=>(
          <Link to={`/topics/${topic.id}`} key={topic.id} className={styles.card}>
            <div className={styles.cardTop}><span>{String(topic.chapterIds.length).padStart(2,"0")}</span>{favorites.includes(topic.id)?<Star size={15} fill="currentColor"/>:null}</div>
            <strong>{topic.title}</strong><small>{topic.chapterIds.length} глав</small><ArrowRight size={17} className={styles.arrow}/>
          </Link>
        ))}</div>
      }
    </div>
  </>;
}