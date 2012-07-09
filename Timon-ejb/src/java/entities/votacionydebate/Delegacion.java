/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entities.votacionydebate;

import entities.registro.Miembro;
import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author alfonso
 */
@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"MIEMBRO_ID","TEMA_ID"}))
public class Delegacion implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    private Miembro miembro;
    @ManyToOne
    private Tema tema;
    @ManyToOne
    private Miembro delegado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Delegacion)) {
            return false;
        }
        Delegacion other = (Delegacion) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.votacionydebate.Delegacion[ id=" + id + " ]";
    }

    /**
     * @return the miembro
     */
    public Miembro getMiembro() {
        return miembro;
    }

    /**
     * @param miembro the miembro to set
     */
    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }

    /**
     * @return the tema
     */
    public Tema getTema() {
        return tema;
    }

    /**
     * @param tema the tema to set
     */
    public void setTema(Tema tema) {
        this.tema = tema;
    }

    /**
     * @return the delegado
     */
    public Miembro getDelegado() {
        return delegado;
    }

    /**
     * @param delegado the delegado to set
     */
    public void setDelegado(Miembro delegado) {
        this.delegado = delegado;
    }
    
}
