INSERT INTO usuarios (id, nombre, correo, clave, rol)
SELECT 1, 'Administrador Cine', 'admin@cineamazonas.pe', 'adae5d133d1e7008af4ecf017bdf9ac8:0bf376fc584cbdb8d797100aabfd3c2c5a4e38162c8307c241866a933e94869f', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 1);

INSERT INTO usuarios (id, nombre, correo, clave, rol)
SELECT 2, 'Cliente Demo', 'cliente@cineamazonas.pe', 'dfd4ab54d4f9405bf1fedcd149485b78:a0532c75b2367f1cd5ab4fff0712ec1a5dcf42a28045ff7f406b09754da5b831', 'USER'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 2);

INSERT INTO generos (id, nombre)
SELECT 1, 'Ciencia ficción' WHERE NOT EXISTS (SELECT 1 FROM generos WHERE id = 1);
INSERT INTO generos (id, nombre)
SELECT 2, 'Animación' WHERE NOT EXISTS (SELECT 1 FROM generos WHERE id = 2);
INSERT INTO generos (id, nombre)
SELECT 3, 'Acción' WHERE NOT EXISTS (SELECT 1 FROM generos WHERE id = 3);

INSERT INTO peliculas (id, nombre, genero_id, minutos, clasificacion, sinopsis, estilo, poster, subtitulo)
SELECT 1, 'Avatar: Fuego y Ceniza', 1, 190, '+14', 'Una nueva aventura en el mundo de Pandora.', 'avatar-card', 'AVATAR', 'Fuego y ceniza'
WHERE NOT EXISTS (SELECT 1 FROM peliculas WHERE id = 1);
INSERT INTO peliculas (id, nombre, genero_id, minutos, clasificacion, sinopsis, estilo, poster, subtitulo)
SELECT 2, 'Super Mario Bros', 2, 92, 'ATP', 'Mario y Luigi viven una gran aventura para salvar el Reino Champiñón.', 'mario-card', 'SUPER', 'Mario Bros.'
WHERE NOT EXISTS (SELECT 1 FROM peliculas WHERE id = 2);
INSERT INTO peliculas (id, nombre, genero_id, minutos, clasificacion, sinopsis, estilo, poster, subtitulo)
SELECT 3, 'Spider-Man', 3, 148, '+14', 'Spider-Man deberá enfrentarse a nuevos enemigos para proteger su ciudad.', 'spider-card', 'SPIDER', 'Man'
WHERE NOT EXISTS (SELECT 1 FROM peliculas WHERE id = 3);

INSERT INTO salas (id, nombre, aforo)
SELECT 1, 'Sala 1', 80 WHERE NOT EXISTS (SELECT 1 FROM salas WHERE id = 1);
INSERT INTO salas (id, nombre, aforo)
SELECT 2, 'Sala 2', 65 WHERE NOT EXISTS (SELECT 1 FROM salas WHERE id = 2);
INSERT INTO salas (id, nombre, aforo)
SELECT 3, 'Sala 3', 90 WHERE NOT EXISTS (SELECT 1 FROM salas WHERE id = 3);

INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 1, 1, 1, CURRENT_DATE, TIME '15:00:00', 15.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 1);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 2, 1, 1, CURRENT_DATE, TIME '19:00:00', 15.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 2);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 3, 2, 2, CURRENT_DATE, TIME '14:00:00', 12.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 3);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 4, 2, 2, CURRENT_DATE, TIME '17:00:00', 12.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 4);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 5, 3, 3, CURRENT_DATE, TIME '16:00:00', 15.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 5);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 6, 3, 3, CURRENT_DATE, TIME '20:00:00', 15.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 6);

INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 7, 1, 1, DATE '2026-09-15', TIME '19:00:00', 15.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 7);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 8, 2, 2, DATE '2026-09-20', TIME '17:00:00', 12.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 8);
INSERT INTO funciones (id, pelicula_id, sala_id, fecha, hora, precio)
SELECT 9, 3, 3, DATE '2026-09-25', TIME '20:00:00', 15.00 WHERE NOT EXISTS (SELECT 1 FROM funciones WHERE id = 9);

INSERT INTO tickets (id, usuario_id, funcion_id, turno, precio, fecha_compra)
SELECT 1, 2, 7, 15, 15.00, DATE '2026-09-15' WHERE NOT EXISTS (SELECT 1 FROM tickets WHERE id = 1);
INSERT INTO tickets (id, usuario_id, funcion_id, turno, precio, fecha_compra)
SELECT 2, 2, 8, 8, 12.00, DATE '2026-09-20' WHERE NOT EXISTS (SELECT 1 FROM tickets WHERE id = 2);
INSERT INTO tickets (id, usuario_id, funcion_id, turno, precio, fecha_compra)
SELECT 3, 2, 9, 21, 15.00, DATE '2026-09-25' WHERE NOT EXISTS (SELECT 1 FROM tickets WHERE id = 3);
