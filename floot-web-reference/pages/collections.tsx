import { Link } from "react-router-dom";
import { ArrowRight, Brain, Droplets, Eye, HeartPulse, Moon, ShieldCheck, Stethoscope, Waves } from "lucide-react";
import { PageHeader } from "../components/PageHeader";
import { useBook } from "../helpers/useBook";
import { buildQuickCollections } from "../helpers/buildQuickCollections";
import styles from "./collections.module.css";

const icons:Record<string,React.ComponentType<{size?:number}>>={
  headache:Brain,
  digestion:Waves,
  sleep:Moon,
  anxiety:HeartPulse,
  ruqyah:ShieldCheck,
  hijama:Droplets,
  eyes:Eye,
  "skin-wounds":Stethoscope,
};

export default function CollectionsPage(){
  const {book,error,loading}=useBook();
  const collections=book?buildQuickCollections(book):[];
  return <>
    <PageHeader title="Быстрые подборки" subtitle="Частые темы в один переход" back/>
    <div className={styles.page}>
      <section className={styles.intro}>
        <span>БЫСТРАЯ НАВИГАЦИЯ</span>
        <h2>Найти нужную тему без поиска по всей книге</h2>
        <p>Подборки не пересказывают книгу, а собирают связанные главы в одном месте.</p>
      </section>
      {loading?<p className={styles.status}>Загрузка…</p>:error?<p className={styles.error}>{error}</p>:
        <div className={styles.grid}>{collections.map((item)=>{
          const Icon=icons[item.id]??Stethoscope;
          return <Link key={item.id} to={"/collections/"+item.id} className={styles.card}>
            <span className={styles.icon}><Icon size={20}/></span>
            <strong>{item.title}</strong>
            <p>{item.description}</p>
            <span className={styles.meta}>{item.chapterIds.length} глав <ArrowRight size={15}/></span>
          </Link>;
        })}</div>}
    </div>
  </>;
}