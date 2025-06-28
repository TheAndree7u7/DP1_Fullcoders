import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Bienvenida.css';

interface Escenario {
  id: string;
  titulo: string;
  descripcion: string;
  imagen: string;
  ruta: string;
  disponible: boolean;
}

const Bienvenida: React.FC = () => {
  const navigate = useNavigate();

  const escenarios: Escenario[] = [
    {
      id: 'ejecucion-real',
      titulo: 'Ejecución en tiempo Real',
      descripcion: 'Monitoreo y control en tiempo real de la flota logística',
      imagen: '/src/assets/camion.svg',
      ruta: '/ejecucion-real',
      disponible: false
    },
    {
      id: 'simulacion-semanal',
      titulo: 'Simulación semanal',
      descripcion: 'Planificación y simulación de operaciones semanales',
      imagen: '/src/assets/almacen_central.svg',
      ruta: '/simulacion-semanal',
      disponible: true
    },
    {
      id: 'colapso-logistico',
      titulo: 'Colapso Logístico',
      descripcion: 'Análisis de escenarios críticos y planes de contingencia',
      imagen: '/src/assets/almacen_intermedio.svg',
      ruta: '/colapso-logistico',
      disponible: false
    }
  ];

  const handleEscenarioClick = (escenario: Escenario) => {
    if (escenario.disponible) {
      navigate(escenario.ruta);
    }
  };

  return (
    <div className="bienvenida-container">
      <header className="bienvenida-header">
        <div className="logo-container">
          <img src="/src/assets/logo.png" alt="GLPSoft" className="logo" />
          <span className="titulo-sistema">GLPSoft</span>
        </div>
        <div className="usuario-info">
          <span className="usuario-nombre">Jhairi Vega</span>
        </div>
      </header>

      <main className="bienvenida-main">
        <div className="bienvenida-content">
          <h1 className="titulo-principal">Selección de Escenarios</h1>
          <p className="subtitulo">Bienvenido al Sistema</p>
          <p className="descripcion">Seleccione un módulo para comenzar.</p>
          
          <div className="escenarios-grid">
            {escenarios.map((escenario) => (
              <div
                key={escenario.id}
                className={`escenario-card ${escenario.disponible ? 'disponible' : 'no-disponible'}`}
                onClick={() => handleEscenarioClick(escenario)}
                role="button"
                tabIndex={escenario.disponible ? 0 : -1}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    handleEscenarioClick(escenario);
                  }
                }}
              >
                <div className="card-header">
                  <h3 className="card-titulo">{escenario.titulo}</h3>
                </div>
                <div className="card-imagen">
                  <img 
                    src={escenario.imagen} 
                    alt={escenario.titulo}
                    className="escenario-imagen"
                  />
                </div>
                {!escenario.disponible && (
                  <div className="overlay-no-disponible">
                    <span>Próximamente</span>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
};

export default Bienvenida;
