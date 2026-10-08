import { Link, useParams } from "react-router-dom";
import { ArrowRight, BookOpen } from "lucide-react";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { buildQuickCollections } from "../helpers/buildQuickCollections";
import styles from "./collections.$collectionId.module.css";

export default function CollectionPage(){
  const {collectionId}=useParams();
  const {book,error,loading}=useBook();
  const collection=book?buildQuickCollections(book).find((item)=>item.id===collectionId):null;
  const chapters=collection?.chapterIds.map((id)=>book!.chapters.find((ch)=>ch.id===id)).filter(Boolean)??[];
  return <>
    <PageHeader title={collection?.title??"Подборка"} back/>
    <div className={styles.page}>
      {loading?<p>Загрузка…</p>:error||!collection?<p className={styles.error}>{error??"Подборка не найдена"}</p>:<>
        <section className={styles.hero}>
          <span>БЫСТРАЯ ПОДБОРКА</span>
          <h2>{collection.title}</h2>
          <p>{collection.description}</p>
        </section>
        <div className={styles.notice}>Все пункты ниже открывают полный текст книги. Подборка служит только удобной навигацией.</div>
        <div className={styles.list}>{chapters.map((chapter)=><Link key={chapter!.id} to={"/read/"+chapter!.slug} className={styles.row}>
          <span className={styles.icon}><BookOpen size={17}/></span>
          <span><strong>{chapter!.title}</strong><small>{chapter!.section}</small></span>
          <ArrowRight size={16}/>
        </Link>)}</div>
      </>}
    </div>
  </>;
}