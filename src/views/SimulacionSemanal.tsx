import Mapa from "../components/Mapa";
import Navbar from "../components/Navbar";
import RightMenu from "../components/RightMenu";

const SimulacionSemanal: React.FC = () => {
  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar />
      <div className="flex flex-row flex-1 overflow-hidden p-4 gap-4">
        <div className="flex-grow overflow-auto">
          <Mapa />
        </div>
        <RightMenu />
      </div>
    </div>
  );
};

export default SimulacionSemanal;
