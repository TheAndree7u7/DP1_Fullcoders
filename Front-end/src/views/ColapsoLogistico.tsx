import React from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { ChevronLeft } from "lucide-react";

const ColapsoLogistico: React.FC = () => {
  const navigate = useNavigate();

  return (
    <div className="bg-[#F5F5F5] w-screen h-screen flex flex-col pt-16">
      <Navbar title="Colapso Logístico" />
      <div className="flex flex-col flex-1 items-center justify-center relative">
        <button
          onClick={() => navigate("/")}
          className="absolute left-4 top-4 z-20 bg-white rounded-full shadow p-2 hover:bg-gray-100 transition"
          title="Volver"
        >
          <ChevronLeft size={24} />
        </button>
        <h2 className="text-xl font-semibold">Colapso Logístico - En desarrollo</h2>
      </div>
    </div>
  );
};

export default ColapsoLogistico;
