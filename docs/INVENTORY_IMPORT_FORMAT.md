# Formato de importacion de inventario

El importador acepta archivos `.csv` y `.xlsx`.

La primera fila se trata como encabezado y se ignora. Cada fila de datos debe tener minimo 8 columnas.

## Columnas reales

| Orden | Columna | Obligatoria | Detalle |
| --- | --- | --- | --- |
| 1 | `sku` | Si | Identificador unico por negocio/sucursal. Si ya existe, actualiza el producto. |
| 2 | `producto` | Si | Nombre visible del producto. |
| 3 | `categoria` | Si | Categoria del producto. |
| 4 | `stock` | Si | Cantidad actual. |
| 5 | `min_stock` | Si | Umbral para alerta de stock bajo. |
| 6 | `proveedor` | Si | Nombre del proveedor. Puede quedar vacio. |
| 7 | `costo` | Si | Costo unitario. |
| 8 | `precio` | Si | Precio unitario de venta. |
| 9 | `estado` | No | Si no viene, se usa `ACTIVO`. |

## Ejemplo CSV

```csv
sku,producto,categoria,stock,min_stock,proveedor,costo,precio,estado
ALM-001,Concentrado Adulto,Alimentos,25,5,Proveedor Uno,42000,58000,ACTIVO
MED-010,Antipulgas 10kg,Medicamentos,8,3,Proveedor Vet,18000,26000,ACTIVO
```
