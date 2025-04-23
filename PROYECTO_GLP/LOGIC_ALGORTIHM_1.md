
PSEUDOCÓDIGO ADAPTADO
PARTE I

// 1.1 Configuración de Spring Boot lee application.properties y MapaConfig
mapaConfig ← new MapaConfig()         // ancho=50, largo=70, distanciaNodos=1

// 1.2 DataLoader.run() se ejecuta al iniciar la app
DataLoader.run():
  1.2.1 loadAlmacenesFromFile("almacenes.txt"):
    leer “data/almacenes/almacenes.txt”
    por cada línea válida:
      crear Almacen entidad con:
        nombre, posX, posY, capacidadGLP, capacidadActualGLP, capacidadMaximaGLP,
        capacidadCombustible, capacidadActualCombustible, capacidadMaximaCombustible,
        esCentral, permiteCamionesEstacionados, horaReabastecimiento, activo
      tipo ← esCentral ? "CENTRAL" : "INTERMEDIO"
      almacenRepository.save(Almacen)
  1.2.2 loadCamionesFromFile("camiones.txt"):
    leer “data/camiones/camiones.txt”
    por cada línea:
      parsear código, tipo, capacidad, cargaActual, tara, combustibleActual, posX, posY, velocidadPromedio, capacidadTanque
      calcular pesoCarga ≈ cargaActual×0.5, pesoCombinado
      Camion c ← new Camion(código, tipo, capacidad, tara)
      asignar resto de atributos
      c.setEstado(DISPONIBLE)
      c.setUltimoAlmacen(almacén central)
      camionRepository.save(c)
  1.2.3 loadMantenimientosFromFile("mantpreventivo.txt"):
    leer “data/mantenimientos/mantpreventivo.txt”
    por cada línea “yyyy-MM-dd” y códigoCamión:
      hallar Camion
      crear Mantenimiento(camión, fechaInicio, fechaFin, "preventivo", estado=0)
      mantenimientoRepository.save(Mantenimiento)
  1.2.4 loadAllPedidos():
    listar archivos ventasYYYYMM.txt
    para cada archivo:
      parsear año, mes
      loadPedidosFromFile(archivo, año, mes):
        por cada línea “##d##h##m:posX,posY,c-id,m3,hH”:
          parsear fechaRegistro, calcular fechaEntregaRequerida = registro + hH
          Cliente cliente ← existing or new (id=c-id, posX, posY)
          Pedido p ← new Pedido()
            p.setCodigo("P"+"%04d")
            p.setCliente(cliente), posX, posY, volumenGLPAsignado, horasLimite, estado=PENDIENTE_PLANIFICACION
            p.setFechaRegistro, fechaEntregaRequerida
          pedidoRepository.save(p)
  1.2.5 loadBloqueosMensuales("aaaamm.bloqueadas"):
    leer “data/bloqueos/aaaamm.bloqueadas”
    parsear fechaInicio, fechaFin, lista de coordenadas
    Bloqueo b ← new Bloqueo(coordenadas, fechaInicio, fechaFin, activo=true)
    bloqueoRepository.save(b)




PARTE I
AffinityPropagation(almacenes, pedidos, λ′, M):
  2.1 Definir nodos = almacenes ∪ clientes de pedidos
  2.2 Para todo i,j en nodos:
        distEsp[i][j] ← |xᵢ−xⱼ| + |yᵢ−yⱼ|            // Manhattan
        distTemp[i][j] ← |ventanaServicio(i)− ventanaServicio(j)|
        S[i][j] ← −α·distEsp[i][j] − β·distTemp[i][j]
  2.3 Inicializar matrices R←0, A←0
  2.4 m ← 1
  2.5 mientras m ≤ M:
        // 2.5.1 Responsabilidad
        para cada i,j:
          R_new[i][j] ← S[i][j] − max_{k≠j}(A[i][k] + S[i][k])
        // 2.5.2 Disponibilidad
        para cada i,j:
          if i≠j:
            A_new[i][j] ← min(0, R_new[j][j] + ∑_{k∉{i,j}} max(0, R_new[k][j]))
          else:
            A_new[j][j] ← ∑_{k≠j} max(0, R_new[k][j])
        // 2.5.3 Amortiguamiento
        R ← λ′·R + (1−λ′)·R_new
        A ← λ′·A + (1−λ′)·A_new
        // 2.5.4 Chequear convergencia de centros
        if centros_estables(R+A):
          break
        m ← m+1
  2.6 Asignar cada cliente al cluster con max_j (A[i][j]+R[i][j])
  2.7 Devolver lista de clusters


Explicando el pseudocódigo de Affinity Propagation
El usuario pide una explicación detallada de las variables en la función AffinityPropagation. Se requieren aclaraciones sobre i, j, los nodos como la unión de almacenes y clientes de pedidos, y qué representan S, R, A, R_new, A_new, α, β, distEsp, distTemp, m, etc. Es importante detallar cómo se construye el array de nodos, qué es la matriz de similitud S, el significado de las variables, el factor de amortiguación λ′, y la verificación de convergencia, especialmente qué se entiende por "centros_estables".
Pensó durante 8 segundos
En esta segunda parte vamos a desmenuzar línea a línea el pseudocódigo de Affinity Propagation, explicando cada símbolo y cada índice.
AffinityPropagation(almacenes, pedidos, λ′, M):
  2.1 Definir nodos = almacenes ∪ clientes de pedidos
  2.2 Para todo i,j en nodos:
        distEsp[i][j] ← |xᵢ − xⱼ| + |yᵢ − yⱼ|            // Manhattan
        distTemp[i][j] ← |ventanaServicio(i) − ventanaServicio(j)|
        S[i][j] ← −α·distEsp[i][j] − β·distTemp[i][j]
  2.3 Inicializar matrices R←0, A←0
  2.4 m ← 1
  2.5 mientras m ≤ M:
        // 2.5.1 Responsabilidad
        para cada i,j:
          R_new[i][j] ← S[i][j] − max_{k≠j}(A[i][k] + S[i][k])
        // 2.5.2 Disponibilidad
        para cada i,j:
          if i≠j:
            A_new[i][j] ← min(0, R_new[j][j] + ∑_{k∉{i,j}} max(0, R_new[k][j]))
          else:
            A_new[j][j] ← ∑_{k≠j} max(0, R_new[k][j])
        // 2.5.3 Amortiguamiento
        R ← λ′·R + (1−λ′)·R_new
        A ← λ′·A + (1−λ′)·A_new
        // 2.5.4 Chequear convergencia de centros
        if centros_estables(R + A):
          break
        m ← m+1
  2.6 Asignar cada cliente al cluster con max_j (A[i][j] + R[i][j])
  2.7 Devolver lista de clusters
________________________________________
❏ 2.1 Definición de “nodos”
•	nodos es un array de tamaño N, donde
N = (# de almacenes) + (# de clientes de todos los pedidos).
•	Cada elemento nodos[p] contiene:
o	xₚ, yₚ: coordenadas en el mapa.
o	ventanaServicioₚ: el intervalo de tiempo en que ese nodo “atiende” pedidos.
Por ejemplo:
o	ventanaServicio(i) = [ 
o	  fechaRegistro(pedido_i), 
o	  fechaEntregaRequerida(pedido_i) 
o	]
o	Si el nodo proviene de un almacén, su ventana es “todo el día” o irrestricta.
________________________________________
❏ Índices i, j, k
•	i, j, k recorren los índices de nodos, es decir valores entre 1 y N.
o	i → representa el nodo “emisor” o cliente que estamos asignando.
o	j → representa el nodo “receptor” o posible centro de cluster.
o	k → índice auxiliar para máximos/sumas que varía también en 1..N, con la restricción de que k ≠ j o k ∉ {i,j} según el contexto.
________________________________________
❏ 2.2 Cálculo de distancias y similitudes
1.	distEsp[i][j]
o	Distancia espacial Manhattan entre nodo i y j:
distEsp[i][j]=∣xi−xj∣+∣yi−yj∣. distEsp[i][j] = |x_i - x_j| + |y_i - y_j|. 
2.	distTemp[i][j]
o	Diferencia en tiempo entre sus ventanas de servicio. Por ejemplo, si i puede atender de 10:00–14:00 y j de 12:00–16:00:
distTemp[i][j]=∣(inicioj+finj)/2−(inicioi+fini)/2∣ distTemp[i][j] = |(inicio_j + fin_j)/2 - (inicio_i + fin_i)/2| 
o	Más general: diferencia absoluta entre tiempos medios o entre inicios, según tu definición.
3.	α, β
o	α y β son coeficientes que ponderan la importancia de la distancia espacial vs. temporal. Deben elegirse según tu prioridad de servicio rápido (β alto) o cercanía (α alto).
4.	S[i][j] (similaridad)
o	Valores altos de similitud indican que i prefiere al nodo j como “centro”.
o	Se define como negativo de una combinación lineal de distancias:
S[i][j]=− α×distEsp[i][j]  −  β×distTemp[i][j]. S[i][j] = -\,α \times distEsp[i][j]\;-\;β \times distTemp[i][j]. 
o	Cuanto más pequeño sea el costo combinado (espacial + temporal), más grande (menos negativo) será S[i][j], favoreciendo la agrupación.
________________________________________
❏ 2.3 Matrices R y A
•	Ambas son matrices N×N, inicializadas a cero:
o	R[i][j] = responsabilidad: refleja cuánto nodo i considera apropiado que j sea su centro.
o	A[i][j] = disponibilidad: refleja cuánto nodo j está disponible o es capaz de ser centro para i.
________________________________________
❏ 2.5 Iteraciones hasta M generaciones
2.5.1 Cálculo de la nueva responsabilidad R_new
Para cada par (i,j):
R_new[i][j] = S[i][j]
             − max_{k ≠ j} ( A[i][k] + S[i][k] )
•	Se compara la similitud con j vs. la mejor alternativa k ≠ j.
•	Si j es mucho mejor que cualquier otro candidato k, R_new[i][j] será alto.
2.5.2 Cálculo de la nueva disponibilidad A_new
Para cada (i,j):
•	Si i ≠ j:
•	A_new[i][j] = min( 0,
•	                   R_new[j][j]
•	                 + ∑_{k ∉ {i,j}} max(0, R_new[k][j]) )
o	R_new[j][j] mide qué tan “apto” se ve j como centro de sí mismo.
o	El sumatorio recoge la aprobación de todos los demás nodos k para que j sea centro.
o	Se toma min(0,…) para no dar demasiada disponibilidad negativa.
•	Si i = j (diagonal):
•	A_new[j][j] = ∑_{k ≠ j} max(0, R_new[k][j])
o	Cuánta “demanda” colectiva tienen los demás nodos para que j sea centro.
2.5.3 Amortiguamiento (damping)
Para evitar oscilaciones abruptas:
R = λ′ * R + (1−λ′) * R_new
A = λ′ * A + (1−λ′) * A_new
•	λ′ ∈ [0,1) es el coeficiente de amortiguamiento (por ejemplo, 0.9).
•	Mezcla la información vieja (R, A) con la nueva (R_new, A_new).
2.5.4 Verificar convergencia de centros
•	centros_estables(R+A) comprueba si el conjunto de índices
arg max⁡j[R[i][j]+A[i][j]] \operatorname{arg\,max}_j \bigl[R[i][j] + A[i][j]\bigr] 
para cada i ha dejado de cambiar respecto a la iteración anterior.
•	Si no cambian, ya no vale la pena seguir iterando → break.
________________________________________
❏ 2.6 Asignación final de clusters
•	Tras la última iteración, cada cliente i se asigna al centro
ci=arg⁡max⁡j(A[i][j]+R[i][j]). c_i = \arg\max_j \bigl( A[i][j] + R[i][j] \bigr). 
•	Los clusters son grupos de todos los i que comparten un mismo c_i.
________________________________________
Resumen de símbolos y dimensiones
Símbolo	Dimensión	Significado
nodos	N	Lista de Almacen ∪ Cliente
i, j, k	1..N	Índices en la lista nodos
distEsp[i][j]	N×N	Distancia Manhattan entre nodos[i] y nodos[j]
distTemp[i][j]	N×N	Diferencia absoluta entre ventanas de servicio de i y j
α, β	escala real	Pesos para espacial vs. temporal
S[i][j]	N×N	Similitud combinada: −α·distEsp − β·distTemp
R, R_new	N×N	Responsabilidades (qué nodo prefiere a qué)
A, A_new	N×N	Disponibilidades (qué nodo es buen centro para qué)
λ′	[0,1)	Coeficiente de amortiguamiento
M	entero > 0	Máximo número de iteraciones
Con esta explicación tienes claro quién es cada variable, qué representa el i, el j, cómo se forman los nodos, y para qué sirve cada matriz en el proceso de Affinity Propagation.
PARTE II
InicializarPoblacion(AP_clusters, N_IND):
  Poblacion ← []
  para p en 1..N_IND:
    cromosoma ← []
    // Cada gen = orden de clientes dentro de clusters
    para cada cluster en AP_clusters:
      listaClientes ← shuffle(cluster.clientes)
      append listaClientes a cromosoma
    Poblacion.add(cromosoma)
  devolver Poblacion

Pensó durante unos pocos segundos
Veámoslo línea a línea:
InicializarPoblacion(AP_clusters, N_IND):
  Poblacion ← []
  para p en 1..N_IND:
    cromosoma ← []
    // Cada gen = orden de clientes dentro de clusters
    para cada cluster en AP_clusters:
      listaClientes ← shuffle(cluster.clientes)
      append listaClientes a cromosoma
    Poblacion.add(cromosoma)
  devolver Poblacion
1.	Entradas
o	AP_clusters: resultado del Affinity Propagation, es una lista de “clusters”. Cada cluster contiene un sub‐conjunto de clientes (cluster.clientes), agrupados por cercanía espacial-temporal.
o	N_IND: número de individuos (soluciones) que queremos en la población inicial del algoritmo genético.
2.	Poblacion ← []
Se crea una lista vacía que almacenará los cromosomas de la población.
3.	Bucle para p en 1..N_IND
Repetimos N_IND veces para generar cada individuo:
3.1. cromosoma ← []
Iniciamos un nuevo cromosoma como lista vacía. Un cromosoma representa una secuencia ordenada de todos los clientes, divididos en “bloques” por cluster.
3.2. Bucle interno para cada cluster en AP_clusters
Recorremos cada grupo obtenido en el clustering:
 - **`cluster.clientes`** es la lista de clientes (IDs o posiciones) pertenecientes a ese cluster.

 - **`shuffle(cluster.clientes)`**  
   Aplica un barajado aleatorio a esa lista de clientes. Así, dentro de cada cluster el orden queda **aleatorio**, fomentando diversidad en la población.

 - **`append listaClientes a cromosoma`**  
   Concatena (añade al final) la lista barajada de clientes al cromosoma.  
   Después de procesar todos los clusters, el cromosoma contendrá **todos** los clientes, en un orden que respeta la **secuencia de clusters**, pero varía dentro de cada uno.
3.3. Poblacion.add(cromosoma)
Añade el cromosoma ya completo (lista de clientes ordenada) a la población.
4.	devolver Poblacion
Al terminar los N_IND bucles, devolvemos la lista de cromosomas que formarán la población inicial del algoritmo genético.
________________________________________
¿Por qué funciona así?
•	Mantener agrupamientos: al respetar el orden por clusters, los vecinos en el cromosoma suelen estar cerca en el mapa, lo cual facilita rutas más cortas.
•	Aleatoriedad interna: el shuffle dentro de cada cluster introduce variación para explorar diferentes permutaciones de clientes.
•	Cobertura total: concatenando todos los clusters garantizamos que cada cliente aparece exactamente una vez en cada cromosoma.
Así obtenemos una población de soluciones iniciales que combina la estructura espacial-temporal dada por AP con una buena dosis de diversidad aleatoria.
PARTE III
AlgoritmoGenético(Poblacion, MAX_GEN, CRUCE, MUTACION):
  gen ← 1
  mientras gen ≤ MAX_GEN:
    // 4.1 Selección (tournament, ruleta…)
    Padres ← Selección(Poblacion)

    // 4.2 Cruce
    Hijos ← []
    para i=1 a Padres.size()/2:
      (padre1, padre2) ← selectPar(Padres)
      if rand()<CRUCE:
        (h1, h2) ← CruceOX(padre1, padre2)     // Order Crossover
      else:
        h1←copia(padre1), h2←copia(padre2)
      Hijos.add(h1, h2)

    // 4.3 Mutación
    para cada h en Hijos:
      if rand()<MUTACION:
        pos1,pos2 ← randPositions(len(h))
        swap(h[pos1], h[pos2])

    // 4.4 Evaluar Fitness multiobjetivo
    Población_Completa ← Poblacion ∪ Hijos
    para cada individuo en Población_Completa:
      // decodificar ruta en rutas Java
      rutasSimuladas ← SimularRutas(individuo, almacenes, camiones, bloqueos, mapaConfig)
      fitness1 ← ConsumoTotal(rutasSimuladas)
      fitness2 ← %PedidosDentroDe4h(rutasSimuladas)
      almacenar (fitness1, −fitness2)   // minimizar consumo, maximizar cumplimiento

    // 4.5 Selección No Dominada + Crowding
    Poblacion ← NoDominatedSortAndCrowding(Población_Completa, tamaño = N_IND)
    gen ← gen+1

  devolver Poblacion    // con g_best en P1[0]
 
 
Variables y Parámetros
•	Poblacion: lista de cromosomas actuales (cada cromosoma es una permutación de clientes agrupados).
•	MAX_GEN: número máximo de generaciones (iteraciones) que ejecutará el algoritmo.
•	CRUCE: probabilidad de aplicar el operador de cruce sobre un par de padres (por ejemplo, 0.8).
•	MUTACION: probabilidad de mutar cada individuo hijo (por ejemplo, 0.1).
•	gen: contador de generación actual, inicializado a 1.
•	Padres, Hijos: listas temporales que almacenan los padres seleccionados y los nuevos hijos generados.
•	Población_Completa: unión de la vieja población y los hijos, usada para la selección de la siguiente generación.
•	fitness1, fitness2: valores de las dos funciones objetivo (consumo total y porcentaje de pedidos entregados a tiempo).
________________________________________
4.1 Selección de Padres
Padres ← Selección(Poblacion)
•	Objetivo: elegir un subconjunto de la población que participe en el cruce.
•	Métodos comunes:
o	Torneo: se eligen aleatoriamente pares (o grupos) de cromosomas y de cada grupo gana el de mejor fitness.
o	Ruleta: cada cromosoma tiene una probabilidad proporcional a su fitness invertido (en minimización) de ser elegido.
•	El resultado es una lista de tamaño igual a Poblacion.size(), emparejable para el cruce.
________________________________________
4.2 Cruce (Crossover)
Hijos ← []
para i=1 a Padres.size()/2:
  (padre1, padre2) ← selectPar(Padres)
  if rand() < CRUCE:
    (h1, h2) ← CruceOX(padre1, padre2)
  else:
    h1 ← copia(padre1)
    h2 ← copia(padre2)
  Hijos.add(h1, h2)
1.	selectPar(Padres):
Extrae dos padres (aleatoriamente o por orden) de la lista de Padres.
2.	rand() < CRUCE:
Genera un número uniforme en [0,1); si es menor que CRUCE, aplicamos cruce, si no, los hijos son copias idénticas.
3.	CruceOX (Order Crossover):
o	Se seleccionan dos puntos de corte en el cromosoma padre.
o	Se copian entre esos puntos los genes de padre1 a h1, y el resto de posiciones se rellenan con el orden de padre2.
o	Se hace simétrico para generar h2.
o	Asegura que cada hijo sea una permutación válida de clientes.
4.	copia(...):
Clona el cromosoma para pasar sin cambios a la siguiente generación.
________________________________________
4.3 Mutación
para cada h en Hijos:
  if rand() < MUTACION:
    pos1, pos2 ← randPositions(len(h))
    swap(h[pos1], h[pos2])
•	rand() < MUTACION: cada hijo tiene probabilidad MUTACION de mutar.
•	randPositions(len(h)): elige dos índices aleatorios en el cromosoma (entre 1 y longitud).
•	swap(...): intercambia los genes en esas dos posiciones.
Esto introduce variación local para escapar de óptimos locales.
________________________________________
4.4 Evaluación Multiobjetivo
Población_Completa ← Poblacion ∪ Hijos
para cada individuo en Población_Completa:
  rutasSimuladas ← SimularRutas(individuo, almacenes, camiones, bloqueos, mapaConfig)
  fitness1 ← ConsumoTotal(rutasSimuladas)
  fitness2 ← %PedidosDentroDe4h(rutasSimuladas)
  almacenar (fitness1, −fitness2)
1.	Población_Completa: combinamos padres e hijos para escoger la próxima generación entre todos.
2.	SimularRutas(...):
o	Toma el orden de clientes (cromosoma) y construye rutas en Java usando tus servicios (RutaService, MapaReticularService, etc.).
o	Considera distancias de Manhattan, bloqueos activos, reabastecimientos y consumos.
3.	fitness1 = ConsumoTotal(...):
Suma el consumo de combustible de todos los camiones en las rutas simuladas (según fórmula: distancia×pesoCombinado/180).
4.	fitness2 = %PedidosDentroDe4h(...):
Calcula el porcentaje de pedidos cuya entrega real estimada cae dentro de la ventana mínima de 4 h.
5.	Almacenamiento:
o	En multiobjetivo queremos minimizar el consumo (fitness1) y maximizar el cumplimiento de tiempo (por eso guardamos −fitness2, para tratarlo como minimización).
________________________________________
4.5 Selección No Dominada + Crowding Distance
Poblacion ← NoDominatedSortAndCrowding(Población_Completa, tamaño = N_IND)
•	No-dominated sort (frentes de Pareto):
o	Divide la población en frentes P₁, P₂, … donde P₁ contiene las soluciones no dominadas (ninguna otra solución es mejor en ambos objetivos), P₂ las dominadas sólo por P₁, etc.
•	Crowding distance:
o	Dentro de cada frente, ordena soluciones según la distancia en espacio de objetivos para preservar la diversidad.
•	Se van llenando la nueva población con los frentes sucesivos hasta alcanzar N_IND individuos; si un frente desborda, se seleccionan los de mayor crowding distance.
________________________________________
Fin de Generación
gen ← gen + 1
hasta que gen>MAX_GEN
•	Se repite el ciclo hasta agotar MAX_GEN.
•	Salida:
o	La variable Poblacion contendrá al final la población de soluciones traducidas a rutas.
o	El mejor individuo (g_best) generalmente queda en la primera posición del primer frente (P₁[0]).
________________________________________
Con esta explicación sabes qué hace cada sección, por qué se eligen esos operadores y cómo encaja con tu sistema Java de simulación de rutas, consumos y restricciones del caso real.
PARTE IV
InsertarPedidosDinamicos(nuevosPedidos):
  para cada pedido en nuevosPedidos:
    soluciones ← []
    para cada camion en camionRepository.findByEstado(DISPONIBLE):
      ruta ← obtenerRutaActual(camion)
      posiciones ← GenerarPuntosInserción(ruta, pedido)   // cada arista
      for pos en posiciones:
        rutaTest ← insertarNodo(ruta, pedido, pos)
        if rutaTest.distanciaTotal ≤ camion.calcularDistanciaMaxima():
          costo ← calcularCosto(rutaTest)  // consumo+demora
          soluciones.add((camion, pos, costo))
    if soluciones.empty:
      camNuevo ← asignarCamionCentral()
      asignar pedido a camNuevo como ruta nueva
    else:
      (camOpt, posOpt, _) ← argmin(soluciones, por costo)
      rutaActual ← ruta de camOpt
      rutaActual.insertarNodo(pedido, posOpt)
      actualizar repositorios
 
________________________________________
1. Entrada
•	nuevosPedidos: lista de objetos Pedido que acaban de llegar y deben insertarse dinámicamente en las rutas ya planificadas.
________________________________________
2. Bucle principal por cada pedido
para cada pedido en nuevosPedidos:
Vamos a intentar insertar uno a uno en alguna ruta existente, sin rehacer todo el plan.
________________________________________
3. Inicializar lista de candidatas
soluciones ← []
soluciones almacenará tuplas (camion, posición_inserción, costo) de cada alternativa factible.
________________________________________
4. Recorrer todos los camiones disponibles
para cada camion en camionRepository.findByEstado(DISPONIBLE):
•	camionRepository.findByEstado(DISPONIBLE) devuelve la lista de objetos Camion cuyo atributo estado == EstadoCamion.DISPONIBLE.
•	Sólo consideramos camiones que no estén ya en mantenimiento, averiados o sin combustible.
________________________________________
5. Obtener la ruta actual del camión
ruta ← obtenerRutaActual(camion)
•	obtenerRutaActual(camion) llama a tu servicio (e.g. RutaService) para recuperar la entidad Ruta en curso asignada a ese camión, con su lista de NodoRuta ordenada.
________________________________________
6. Generar todos los puntos de inserción posibles
posiciones ← GenerarPuntosInserción(ruta, pedido)   // cada arista
•	Cada “pos” representa un arista de la ruta (entre dos nodos consecutivos).
•	GenerarPuntosInserción recorre la lista de nodos de ruta y devuelve todos los índices posibles donde insertar el nuevo nodo (cliente):
o	Ejemplo: si la ruta tiene nodos [N0, N1, N2], las posiciones válidas son pos=1 (entre N0→N1) y pos=2 (entre N1→N2), y pos=3 (antes de volver al almacén final).
________________________________________
7. Probar cada inserción
for pos en posiciones:
  rutaTest ← insertarNodo(ruta, pedido, pos)
•	insertarNodo(ruta, pedido, pos) construye una copia de la ruta original (rutaTest) e inserta allí el nuevo pedido como un NodoRuta en la posición pos.
•	Esto recalcula internamente la distanciaTotal de rutaTest usando la distancia Manhattan entre nodos.
________________________________________
8. Filtrar por autonomía de combustible
if rutaTest.distanciaTotal ≤ camion.calcularDistanciaMaxima():
•	camion.calcularDistanciaMaxima() devuelve la distancia máxima que el camión puede recorrer con el combustible actual:
distanciaMaxima=combustibleActual (gal)pesoCombinado (ton)/180 \text{distanciaMaxima} = \frac{\text{combustibleActual (gal)}}{\text{pesoCombinado (ton)} / 180} 
•	Solo consideramos la inserción si rutaTest.distanciaTotal no excede esa autonomía.
________________________________________
9. Calcular costo de la ruta de prueba
costo ← calcularCosto(rutaTest)  // consumo+demora
•	calcularCosto(rutaTest) es una función que combina:
1.	Consumo total de combustible (suma de consumos tramo a tramo según fórmula).
2.	Demora (por ejemplo, penaliza rutas que alargan más del límite de 4 h).
•	Devuelve un valor escalar para comparar alternativas.
________________________________________
10. Almacenar solución candidata
soluciones.add((camion, pos, costo))
•	Añadimos la tupla solo si la autonomía lo permite.
________________________________________
11. ¿No hay soluciones factibles?
if soluciones.empty:
•	Si ningún camión pudo insertar el pedido sin perder la autonomía:
11.1. Asignar un camión nuevo desde la planta central
camNuevo ← asignarCamionCentral()
asignar pedido a camNuevo como ruta nueva
•	asignarCamionCentral() obtiene un camión desde el almacén central (puede significar “reserva” o “crear nueva ruta vacía”).
•	Se crea una nueva ruta que empieza en el almacén central e incluye solo este pedido.
________________________________________
12. Si hay soluciones, elegir la mejor
else:
  (camOpt, posOpt, _) ← argmin(soluciones, por costo)
•	argmin(soluciones, por costo) selecciona la tupla con menor costo.
o	camOpt: camión óptimo.
o	posOpt: posición de inserción óptima.
________________________________________
13. Insertar en la ruta real y persistir
rutaActual ← ruta de camOpt
rutaActual.insertarNodo(pedido, posOpt)
actualizar repositorios
1.	Recuperar la ruta activa en BD para camOpt.
2.	Llamar a un método de la entidad (o del servicio) para insertar el nodo del nuevo pedido en posOpt.
3.	Guardar (save) la entidad Ruta actualizada y el Pedido asignado en la base de datos para persistir la nueva planificación.
________________________________________
Resumen del flujo
1.	Recorremos cada pedido dinámico.
2.	Para cada camión disponible, generamos todas las formas de insertar el pedido en su ruta actual (probar cada arista).
3.	Filtramos por autonomía y calculamos un costo que combina consumo y demora.
4.	Si ningún camión puede atenderlo, sacamos uno nuevo de la planta central.
5.	Si existen opciones, elegimos la menor según costo y hacemos la inserción persistente.
Con este mecanismo dinámico incorporas nuevos pedidos en caliente sin recalcular toda la planificación, maximizando la eficiencia del combustible y minimizando retrasos.
PARTE V
ConstruirYGuardarRutas(mejorIndividuo):
  rutasDTO ← decodeSolución(mejorIndividuo)
  for cada rutaDTO en rutasDTO:
    Ruta rutaEnt ← new Ruta(codigo := UUID(), fechaCreacion := now())
    rutaEnt.setCamion(camionRepository.findByCodigo(rutaDTO.camionCodigo))
    for cada punto en rutaDTO.puntos:
      if punto.tipo=="ALMACEN":
        rutaEnt.agregarNodo(punto.posX, punto.posY, "ALMACEN")
      else:
        pedidoEnt ← pedidoRepository.findById(punto.idPedido)
        rutaEnt.agregarNodoCliente(pedidoEnt.posX, pedidoEnt.posY,
                                  pedidoEnt, punto.volumenGLP, punto.porcentaje)
    rutaEnt.calcularDistanciaTotal()
    rutaRepository.save(rutaEnt)
 
________________________________________
1. Decodificar la solución genética
rutasDTO ← decodeSolución(mejorIndividuo)
•	mejorIndividuo es el cromosoma óptimo extraído de la población final (una lista ordenada de IDs de clientes agrupados por rutas y camiones).
•	decodeSolución(...) transforma ese cromosoma en una lista de objetos RutaDTO.
Cada RutaDTO contiene:
o	camionCodigo: cadena con el código del camión asignado.
o	puntos: lista ordenada de PuntoRutaDTO con campos { tipo, posX, posY, idPedido?, volumenGLP?, porcentaje? }.
________________________________________
2. Iterar sobre cada ruta a crear
for cada rutaDTO en rutasDTO:
Para cada objeto de transferencia (RutaDTO) generamos una entidad JPA Ruta.
________________________________________
3. Construir la entidad Ruta
Ruta rutaEnt ← new Ruta(codigo := UUID(), fechaCreacion := now())
•	Se instancia una nueva Ruta:
o	codigo: identificador único generado con UUID().
o	fechaCreacion: marca temporal de ahora (now()).
________________________________________
4. Asignar el camión a la ruta
rutaEnt.setCamion(camionRepository.findByCodigo(rutaDTO.camionCodigo))
•	camionRepository.findByCodigo(...) busca la entidad Camion en la BD cuyo código coincide con rutaDTO.camionCodigo.
•	Se vincula esa entidad al campo rutaEnt.camion.
________________________________________
5. Agregar nodos a la ruta
for cada punto en rutaDTO.puntos:
Recorremos la secuencia de puntos que definió el decodeSolución.
5.1 Si el punto es un almacén
if punto.tipo=="ALMACEN":
  rutaEnt.agregarNodo(punto.posX, punto.posY, "ALMACEN")
•	agregarNodo(x,y,"ALMACEN") crea un NodoRuta de tipo "ALMACEN" en la posición (posX,posY).
•	Es el punto de partida o retorno a la planta/intermedio.
5.2 Si el punto es un cliente
else:
  pedidoEnt ← pedidoRepository.findById(punto.idPedido)
  rutaEnt.agregarNodoCliente(
      pedidoEnt.posX, pedidoEnt.posY,
      pedidoEnt, punto.volumenGLP, punto.porcentaje)
•	pedidoRepository.findById(...) recupera la entidad Pedido con ese id.
•	agregarNodoCliente(x,y,pedido,vol,%):
o	Crea un NodoRuta de tipo "CLIENTE".
o	Asocia la referencia al Pedido para mantener trazabilidad.
o	Registra el volumen GLP y el porcentaje de entrega parcial.
o	Internamente suma ese volumen al total de la ruta y actualiza el porcentaje de uso del camión.
________________________________________
6. Calcular la distancia total de la ruta
rutaEnt.calcularDistanciaTotal()
•	Recorre todos los NodoRuta en orden y suma las distancias Manhattan entre nodos consecutivos, guardando el resultado en rutaEnt.distanciaTotal.
________________________________________
7. Persistir la ruta en la base de datos
rutaRepository.save(rutaEnt)
•	Guarda (o actualiza) la entidad Ruta junto con sus NodoRuta en la base de datos.
•	Al ser cascada (CascadeType.ALL), también persiste automáticamente los nodos vinculados.
________________________________________
Flujo completo
1.	Decodificar la mejor solución genética en DTOs legibles.
2.	Por cada ruta:
o	Crear entidad Ruta con código y fecha.
o	Relacionar al Camion correspondiente.
o	Por cada punto de la ruta, añadir un nodo (almacén o cliente).
o	Calcular la distancia total (para métricas y validaciones posteriores).
o	Guardar en la BD.
Con este procedimiento conviertes la representación abstracta del cromosoma en objetos JPA reales, listos para usarse en simulaciones, visualización o repuestas REST.
