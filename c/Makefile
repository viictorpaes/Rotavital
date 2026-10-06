CC = gcc
CFLAGS = -Wall -Wextra -std=c99 -pedantic -g
TARGET = rotavital_ed

SRCS = main.c rotavital_tipos.c lista_estoque.c fila_requisicoes.c pilha_operacoes.c
OBJS = $(SRCS:.c=.o)

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) -o $(TARGET) $(OBJS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

run: $(TARGET)
	./$(TARGET)

clean:
	rm -f $(OBJS) $(TARGET) $(TARGET).exe

