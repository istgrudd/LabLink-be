# --- TAHAP 1: MEMASAK (BUILD) ---
# Kita pinjam image Maven yang sudah ada Java 17-nya
FROM maven:3.8.5-openjdk-17 AS build

# Bikin folder kerja di dalam Docker
WORKDIR /app

# Copy semua kodingan dari laptop ke dalam Docker
COPY . .

# Perintah untuk "memasak" aplikasi jadi file .jar
# (-DskipTests biar cepat, tidak perlu jalankan testing dulu)
RUN mvn clean package -DskipTests

# --- TAHAP 2: MENYAJIKAN (RUN) ---
# Kita pindahkan hasil masakan ke piring yang lebih kecil (Linux ringan)
# Biar size servernya hemat dan downloadnya cepat
FROM openjdk:17-jdk-slim

WORKDIR /app

# Ambil file .jar yang sudah matang dari Tahap 1 tadi
COPY --from=build /app/target/*.jar app.jar

# Beritahu Railway/Render bahwa pintu masuknya di port 8080
EXPOSE 8080

# Perintah untuk menyalakan aplikasi saat server hidup
ENTRYPOINT ["java", "-jar", "app.jar"]