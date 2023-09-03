# Merkezi Birim
## Hakkında
Merkezi birim sensörlerden aldığı bilgileri işleyerek hedefin koordinat sisteminde konumlandığı noktayı hesaplayarak sergileyecektir.
## Gereklilikler
- Java 17
- Kafka Sunucu 3.5.1
## Çalıştırma
- İlk olarak Kafka Sunucu çalıştırılır.
- Daha sonra havelsan_central_unit konsol üzerinden çalıştırılır.
- Merkezi birim sensör birimlerinden kerteriz değerlerini ve kendi kordinat değerlerini bekler.
- İki sensör yazılımından veri geldiği an kordinat hesaplamasını yaparak konsol ekranına yazdırır.
