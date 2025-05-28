package com.plg.utils;

public class ExcepcionesPerzonalizadas {
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }

        public ResourceNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidInputException extends RuntimeException {
        public InvalidInputException(String message) {
            super(message);
        }

        public InvalidInputException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidDataFormatException extends Exception {
        public InvalidDataFormatException(String message) {
            super(message);
        }

        public InvalidDataFormatException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
