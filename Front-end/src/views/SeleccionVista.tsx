import React from "react";
import {
  Header,
  Footer,
  StatusMessage,
  SimulacionGrid,
  InformacionSistema,
  GestionPedidos,
  Bienvenida,
  useSeleccionVista,
  opcionesSimulacion
} from "../components/seleccion-vista";

const SeleccionVista: React.FC = () => {
  const {
    cargando,
    mensaje,
    handleSeleccionVista,
    handleNavegarAPedidos
  } = useSeleccionVista();

  return (
    <div className="bg-gradient-to-br from-gray-50 to-gray-100 w-screen h-screen flex flex-col">
      {/* Header */}
      <Header />

      {/* Contenido principal */}
      <div className="flex-1 flex flex-col justify-center items-center p-8">
        {/* Mensaje de bienvenida */}
        <Bienvenida />

        {/* Mensaje de estado */}
        <StatusMessage mensaje={mensaje} />

        {/* Opciones de simulación */}
        <SimulacionGrid
          opciones={opcionesSimulacion}
          cargando={cargando}
          onSeleccionar={handleSeleccionVista}
        />

        {/* Información adicional */}
        <InformacionSistema />

        {/* Botón para agregar pedidos */}
        <GestionPedidos onNavegar={handleNavegarAPedidos} />
      </div>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default SeleccionVista;
