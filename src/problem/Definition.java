package problem;

import java.util.*;

public class Definition {
    /**
     * Cantidad de destinos
     */
    private int amountDestinations;

    /**
     * Cantidad de destinos priorizados
     */
    private int amountPrioritized;

    /**
     * Velocidad a la que se realizara la ruta
     */
    private float routeSpeed;

    /**
     * Indica que destinos son priorizados (true en el indice correspondiente)
     */
    private Boolean[] prioritized;

    /**
     * Costo de desplazarse de una posicion i a una posicion j
     */
    private float[][] transitionsCost;

    /**
     * Semilla para la generacion de numeros aleatorios
     */
    private long seed;

    /**
     * Generador de numeros aleatorios
     */
    private Random randomGenerator;

    /**
     * Instancia singleton de la clase
     */
    private static Definition definition;

    /**
     * Constructor por defecto privado
     */
    private Definition() {

    }

    /**
     * Metodo para obtener la instancia singleton
     */
    public static Definition getDefinition() {
        if (definition == null) {
            definition = new Definition();
        }
        return definition;
    }

    /**
     * Generador de instancias aleatorias
     */
    public void randomInstanceGeneration(int seed, int amountDestinations, float routeSpeed) {
        this.seed = seed;
        this.randomGenerator = new Random(seed);
        //Inicializacion de cantidades
        this.setAmountDestinations(amountDestinations);
        this.setAmountPrioritized(amountDestinations / 2);
        this.setRouteSpeed(routeSpeed);
        //Inicializacion de colecciones
        this.transitionsCost = new float[amountDestinations][amountDestinations];
        this.prioritized = new Boolean[amountDestinations];
        //Lista auxiliar para generacion de destinos priorizados
        List<Boolean> prior = new ArrayList<>();

        //Generacion de la matriz de distancias
        for (int i = 0; i < this.amountDestinations; i++) {
            for (int j = 0; j < i; j++) {
                //Coste aleatorio entre los estados
                float generated = randomGenerator.nextFloat() * 10;
                //La matriz de costos es simetrica
                transitionsCost[i][j] = generated;
                transitionsCost[j][i] = generated;
            }
            //se agrega el maximo valor a la diagonal principal para no tomar en cuenta los lazos
            transitionsCost[i][i] = Float.MAX_VALUE;
        }
        //Genera la mitad de destinos como productos priorizados
        for (int i = 0; i < this.amountDestinations / 2; i++) {
            //Itera agregando parejas true/false a la lista, obteniendo la misma cantidad de ambos
            prior.add(true);
            prior.add(false);
        }
        //Si la cantidad de destinos es impar se agrega uno mas dado que el bucle solo agrega la cantidad par inferior
        if (amountDestinations % 2 > 0) prior.add(false);

        //Aleatoriza los destinos priorizados
        Collections.shuffle(prior, randomGenerator);

        //Garantiza que la primera posicion (origen) no este marcada como priorizado
        if (prior.get(0)) {
            prior.set(prior.indexOf(false), true);
            prior.set(0, false);
        }
        //Almacena los indices de los productos priorizados
        this.prioritized = prior.toArray(prioritized);
    }

    /**
     * Determina si el producto de una posicion es priorizado
     */
    public boolean isPrioritized(int dest) {
        return prioritized[dest];
    }

    /**
     * Obtiene el costo de transicion entre dos posiciones dadas
     */
    public float getCostBetween(int i, int j) {
        return transitionsCost[i][j];
    }

    /**
     * Gets y sets
     */

    public int getAmountDestinations() {
        return amountDestinations;
    }

    public void setAmountDestinations(int amountDestinations) {
        this.amountDestinations = amountDestinations;
    }

    public int getAmountPrioritized() {
        return amountPrioritized;
    }

    public void setAmountPrioritized(int amountPrioritized) {
        this.amountPrioritized = amountPrioritized;
    }

    public float getRouteSpeed() {
        return routeSpeed;
    }

    public void setRouteSpeed(float routeSpeed) {
        if (routeSpeed > 0) this.routeSpeed = routeSpeed;
        else throw new IllegalArgumentException("La velocidad debe ser mayor que 0");
    }

    public Boolean[] getPrioritized() {
        return prioritized;
    }

    public void setPrioritized(Boolean[] prioritized) {
        this.prioritized = prioritized;
    }

    public float[][] getTransitionsCost() {
        return transitionsCost;
    }

    public void setTransitionsCost(float[][] transitionsCost) {
        this.transitionsCost = transitionsCost;
    }

    public Random getRandomGenerator() {
        return randomGenerator;
    }
}
