
PFont helvetica1;
PFont helvetica2;

PImage b;



Voter v;
float rot;
int vuelta;
float step = 1;
float tick = 10;
int z = 0;

void setup() {
  size(970, 300);
  b = loadImage("http://wikipartido.mx/resources/loaders/test2/fondo","png");
  //b = loadImage("/Users/alfonso/NetBeansProjects/timon/Timon-war/web/resources/loaders/test2/fondo.png");
  v = new Voter();
  background(190, 190, 190, 50);
  helvetica1 = createFont("Helvetica", 24);
  helvetica2 = createFont("Helvetica", 12);
  
  smooth();
  stroke(0);
  frameRate(24);
}

void draw() {
  image(b, 0, 0);
  pushMatrix();
  translate(224+width/2, height/2);
  rotate(rot);
  v.display();  
  popMatrix();
  fill(180);
  textFont(helvetica1);
  text("CONTANDO LOS VOTOS",25,40);
  textFont(helvetica2);
  text("El sistema realizará el computo de la elección en caso de que se haya realizado antes o si se acaba "
  +"de registrar un voto. En votaciones con pocas opciones el conteo requiere algunos segundos pero si se presentan varias decenas de opciones "
  +"y varios miles de electores, el conteo puede llevar algunos minutos por lo que le pedimos su paciencia. ",25,70,380,250);

  rot = rot +0.02;
  if (rot > (TWO_PI)) {
    rot=0;
    vuelta++;
  }
  tick = tick+ 0.2;
}


class Voter {
  int n;
  float r;
  Punto[] p;
  float rot=0;
  Voter() {
    n = int(random(5, 20));
    r = 120;
    p = new Punto[n];
    float th=0;
    for (int i=0; i<n; i++) {    
      float x = (r * cos(th));
      float y = (r * sin(th));
      th += (TWO_PI)/n;
      p[i] = new Punto(x, y);
    }
  }
  void display() {  
    if (tick > step) {
      z++;
      tick=0;
    }
    if (z <=n) {
      for (int i=0; i<z; i++) {
        for (int j=0; j<z; j++) {
          stroke(random(50,200), 100);
          strokeWeight(7);
          line(p[i].x, p[i].y, p[j].x, p[j].y);
        }
      }
    }
    if (z>=n+1) {
      z=0;
      setup();
    }
    stroke(0);
    strokeWeight(1);
    fill(#651759); 
    for (int i=0; i<n; i++) {   
      ellipse(p[i].x, p[i].y, 20, 20);
    }
    fill(255);
  }
}


class Punto {
  float x;
  float y;
  Punto(float x, float y) {
    this.x = x;
    this.y = y;
  }
}


void mouseClicked() {
  setup();
}
