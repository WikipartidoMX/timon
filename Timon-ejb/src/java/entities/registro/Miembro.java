/*
 *   __    __ _ _     _   ___           _   _     _             __  __
 *  / / /\ \ (_) | __(_) / _ \__ _ _ __| |_(_) __| | ___   /\/\ \ \/ /
 *  \ \/  \/ / | |/ /| |/ /_)/ _` | '__| __| |/ _` |/ _ \ /    \ \  / 
 *   \  /\  /| |   < | / ___/ (_| | |  | |_| | (_| | (_) / /\/\ \/  \ 
 *    \/  \/ |_|_|\_\|_\/    \__,_|_|   \__|_|\__,_|\___/\/    \/_/\_\
 *                                              
 *                                              
 *  
 * Copyright 2012 Wikipartido de Mexico, A.C.
 * VER ARCHIVO DE LiCENCIA
 * 
 * 
 */
package entities.registro;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author Alfonso Tames
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"EMAIL"})})
public class Miembro implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date nacimiento;
    private String claveDeElector;
    private String curp;
    private Long seccion;
    @ManyToOne
    private Estado estado;
    private String calle;
    private String numeroExterior;
    private String numeroInterior;
    private String cp;
    private String colonia;
    private String municipio;
    private String telefono;
    private String ocupacion;
    private boolean formaEntregada;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date fechaEntregaForma;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date fechaRegistro;
    private String ip;
    private String twitter;
    private String url;
    private String clavedeusuario;
    private Long paso; // En que paso esta 1. Wiki 2. Afiliado


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

        if (!(object instanceof Miembro)) {
            return false;
        }
        Miembro other = (Miembro) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entidades.Miembro[ id=" + id + " ]";
    }
    
    public String getSexo() {
        String s = "H";
        String l = claveDeElector.substring(claveDeElector.length()-4, claveDeElector.length()-3);
        if (l.equals("M") || l.equals("m")) {
            s = "M";
        }
        return s;
    }
    
    public String getNombreCompleto() {
        return nombre + " " + apellidoPaterno + " " + apellidoMaterno;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the apellidoPaterno
     */
    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    /**
     * @param apellidoPaterno the apellidoPaterno to set
     */
    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    /**
     * @return the apellidoMaterno
     */
    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    /**
     * @param apellidoMaterno the apellidoMaterno to set
     */
    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    /**
     * @return the nacimiento
     */
    public Date getNacimiento() {
        return nacimiento;
    }

    /**
     * @param nacimiento the nacimiento to set
     */
    public void setNacimiento(Date nacimiento) {
        this.nacimiento = nacimiento;
    }

    /**
     * @return the claveDeElector
     */
    public String getClaveDeElector() {
        return claveDeElector;
    }

    /**
     * @param claveDeElector the claveDeElector to set
     */
    public void setClaveDeElector(String claveDeElector) {
        this.claveDeElector = claveDeElector;
    }

    /**
     * @return the curp
     */
    public String getCurp() {
        return curp;
    }

    /**
     * @param curp the curp to set
     */
    public void setCurp(String curp) {
        this.curp = curp;
    }

    /**
     * @return the seccion
     */
    /**
     * @return the calle
     */
    public String getCalle() {
        return calle;
    }

    /**
     * @param calle the calle to set
     */
    public void setCalle(String calle) {
        this.calle = calle;
    }

    /**
     * @return the numeroExterior
     */
    public String getNumeroExterior() {
        return numeroExterior;
    }

    /**
     * @param numeroExterior the numeroExterior to set
     */
    public void setNumeroExterior(String numeroExterior) {
        this.numeroExterior = numeroExterior;
    }

    /**
     * @return the numeroInterior
     */
    public String getNumeroInterior() {
        return numeroInterior;
    }

    /**
     * @param numeroInterior the numeroInterior to set
     */
    public void setNumeroInterior(String numeroInterior) {
        this.numeroInterior = numeroInterior;
    }

    /**
     * @return the cp
     */
    public String getCp() {
        return cp;
    }

    /**
     * @param cp the cp to set
     */
    public void setCp(String cp) {
        this.cp = cp;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * @return the ocupacion
     */
    public String getOcupacion() {
        return ocupacion;
    }

    /**
     * @param ocupacion the ocupacion to set
     */
    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }

    /**
     * @return the formaEntregada
     */
    public boolean isFormaEntregada() {
        return formaEntregada;
    }

    /**
     * @param formaEntregada the formaEntregada to set
     */
    public void setFormaEntregada(boolean formaEntregada) {
        this.formaEntregada = formaEntregada;
    }

    /**
     * @return the fechaEntregaForma
     */
    public Date getFechaEntregaForma() {
        return fechaEntregaForma;
    }

    /**
     * @param fechaEntregaForma the fechaEntregaForma to set
     */
    public void setFechaEntregaForma(Date fechaEntregaForma) {
        this.fechaEntregaForma = fechaEntregaForma;
    }

    /**
     * @return the fechaRegistro
     */
    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * @param fechaRegistro the fechaRegistro to set
     */
    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the twitter
     */
    public String getTwitter() {
        return twitter;
    }

    /**
     * @param twitter the twitter to set
     */
    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    /**
     * @return the clavedeusuario
     */
    public String getClavedeusuario() {
        return clavedeusuario;
    }

    /**
     * @param clavedeusuario the clavedeusuario to set
     */
    public void setClavedeusuario(String clavedeusuario) {
        this.clavedeusuario = clavedeusuario;
    }

    /**
     * @return the colonia
     */
    public String getColonia() {
        return colonia;
    }

    /**
     * @param colonia the colonia to set
     */
    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    /**
     * @return the municipio
     */
    public String getMunicipio() {
        return municipio;
    }

    /**
     * @param municipio the municipio to set
     */
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    /**
     * @return the seccion
     */
    public Long getSeccion() {
        return seccion;
    }

    /**
     * @param seccion the seccion to set
     */
    public void setSeccion(Long seccion) {
        this.seccion = seccion;
    }

    /**
     * @return the estado
     */
    public Estado getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    /**
     * @return the paso
     */
    public Long getPaso() {
        return paso;
    }

    /**
     * @param paso the paso to set
     */
    public void setPaso(Long paso) {
        this.paso = paso;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }


}
