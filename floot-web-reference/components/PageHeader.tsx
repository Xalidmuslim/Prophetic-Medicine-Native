import { ArrowLeft, Settings2 } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "./Button";
import styles from "./PageHeader.module.css";

export const PageHeader = ({title,subtitle,back=false,action}:{title:string;subtitle?:string;back?:boolean;action?:React.ReactNode}) => {
  const navigate = useNavigate();
  return (
    <header className={styles.header}>
      <div className={styles.inner}>
        {back ? <Button variant="ghost" size="icon-md" aria-label="Назад" onClick={() => navigate(-1)}><ArrowLeft size={21}/></Button> : <div className={styles.spacer}/>}
        <div className={styles.titles}>
          <h1>{title}</h1>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        <div className={styles.actions}>{action ?? <Button asChild variant="ghost" size="icon-md" aria-label="Настройки"><Link to="/settings"><Settings2 size={18}/></Link></Button>}</div>
      </div>
    </header>
  );
};