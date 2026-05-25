# Makefile para el proyecto Tetris Modular
# Organizado para una compilación limpia y eficiente.

CXX = g++
CXXFLAGS = -Wall -Wextra -std=c++11
LDFLAGS = -lncurses

# Archivos del proyecto
TARGET = tetris
SRCS = main.cpp tetris.cpp
OBJS = $(SRCS:.cpp=.o)

# Regla principal: Compilar el ejecutable
$(TARGET): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(OBJS) $(LDFLAGS)

# Regla para compilar archivos objeto (.o) desde fuentes (.cpp)
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

# Limpiar archivos temporales y ejecutable
clean:
	rm -f $(OBJS) $(TARGET) highscore.txt

# Regla para forzar la recompilación
re: clean all

.PHONY: all clean re
