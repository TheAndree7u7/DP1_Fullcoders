import checkIcon from '../assets/checkIcon.svg';
import gasolinaIcon from '../assets/gasolinaIcon.svg';

const MetricasRendimiento: React.FC = () => {
  return (
    <div className="flex flex-row gap-4 justify-center">
      <div className="flex flex-col">
        <img src={checkIcon} alt="Check icon" className="w-[42px] h-[46px]" />

      </div>

      <div className="flex flex-col">
        <img src={gasolinaIcon} alt="Check icon" className="w-[42px] h-[46px]" />

      </div>
    </div>
  );
};

export default MetricasRendimiento;
