
PFont helvetica12;
PImage b;



Voter v;
float rot;
int vuelta;
float step = 1;
float tick = 10;
int z = 0;

void setup() {
  size(970, 300);
  b = loadImage("http://localhost:8080/Timon-war/resources/loaders/test2/fondo","png");
  v = new Voter();
  background(190, 190, 190, 50);
  helvetica12 = createFont("Helvetica", 12);
  textFont(helvetica12);
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
  text("rot="+rot, 10, 20);
  text("vuelta="+vuelta, 10, 40);
  text("tick="+tick, 10, 60);

  text("z="+z, 10, 80);
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
