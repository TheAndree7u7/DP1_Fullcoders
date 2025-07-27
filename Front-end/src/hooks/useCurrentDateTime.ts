import { useState, useEffect } from 'react';

export const useCurrentDateTime = () => {
  const [currentDateTime, setCurrentDateTime] = useState(new Date());

  useEffect(() => {
    let isMounted = true;
    
    const interval = setInterval(() => {
      if (isMounted) {
        setCurrentDateTime(new Date());
      }
    }, 1000);

    return () => {
      isMounted = false;
      clearInterval(interval);
    };
  }, []);

  return currentDateTime;
}; 