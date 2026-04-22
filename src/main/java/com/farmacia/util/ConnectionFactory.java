package com.farmacia.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {
    private static final String URL = "jdbc:sqlite:farmacia.db";
    private static final String VERSAO_CATALOGO = "catalogo-2026-04-21-v7";
    private static final String FORNECEDOR_SANTA_CRUZ_CNPJ = "61940292007301";
    private static boolean bancoInicializado = false;

    private static final String[] CATALOGO_PRODUTOS = {
            "Aciclovir 200mg Comprimidos",
            "Aciclovir 400mg Comprimidos",
            "Aciclovir Creme Dermatologico 10g",
            "Aciclovir Pomada Oftalmica 3,5g",
            "Acido Acetilsalicilico 100mg Comprimidos",
            "Acido Acetilsalicilico 500mg Comprimidos",
            "Acido Folico 5mg Comprimidos",
            "Acido Valproico 250mg Capsulas",
            "Acido Valproico 500mg Capsulas",
            "Albendazol 400mg Comprimidos Mastigaveis",
            "Albendazol 40mg/ml Suspensao Oral 10ml",
            "Alendronato de Sodio 70mg Comprimidos",
            "Alprazolam 0,5mg Comprimidos",
            "Alprazolam 1mg Comprimidos",
            "Amoxicilina 500mg Capsulas",
            "Amoxicilina 875mg Comprimidos",
            "Amoxicilina 250mg/5ml Suspensao Oral 150ml",
            "Amoxicilina 400mg/5ml Suspensao Oral 70ml",
            "Amoxicilina + Clavulanato 500mg + 125mg Comprimidos",
            "Amoxicilina + Clavulanato 875mg + 125mg Comprimidos",
            "Amoxicilina + Clavulanato 250mg/5ml + 62,5mg/5ml Suspensao 75ml",
            "Amoxicilina + Clavulanato 400mg/5ml + 57mg/5ml Suspensao 70ml",
            "Amitriptilina 25mg Comprimidos",
            "Amitriptilina 75mg Comprimidos",
            "Anlodipino 5mg Comprimidos",
            "Anlodipino 10mg Comprimidos",
            "Atenolol 25mg Comprimidos",
            "Atenolol 50mg Comprimidos",
            "Atenolol 100mg Comprimidos",
            "Azitromicina 500mg Comprimidos",
            "Azitromicina 600mg Comprimidos",
            "Azitromicina 200mg/5ml Suspensao Oral 15ml",
            "Azitromicina 200mg/5ml Suspensao Oral 22,5ml",
            "Bromazepam 3mg Comprimidos",
            "Bromazepam 6mg Comprimidos",
            "Bromoprida 10mg Comprimidos",
            "Bromoprida 4mg/ml Gotas 20ml",
            "Bromoprida 1mg/ml Solucao Oral 120ml",
            "Budesonida 32mcg Spray Nasal 120 Doses",
            "Budesonida 50mcg Spray Nasal 120 Doses",
            "Captopril 25mg Comprimidos",
            "Captopril 50mg Comprimidos",
            "Carbamazepina 200mg Comprimidos",
            "Carbamazepina 400mg Comprimidos",
            "Carbonato de Calcio + Vitamina D Comprimidos",
            "Carvedilol 3,125mg Comprimidos",
            "Carvedilol 6,25mg Comprimidos",
            "Carvedilol 12,5mg Comprimidos",
            "Carvedilol 25mg Comprimidos",
            "Cefalexina 500mg Capsulas",
            "Cefalexina 250mg/5ml Suspensao Oral 100ml",
            "Cetoconazol 200mg Comprimidos",
            "Cetoconazol Creme Dermatologico 30g",
            "Cetoconazol Shampoo 100ml",
            "Cetirizina 10mg Comprimidos",
            "Cetirizina 1mg/ml Xarope 120ml",
            "Cetirizina 1mg/ml Solucao Oral Infantil 120ml",
            "Cinarizina 25mg Comprimidos",
            "Cinarizina 75mg Comprimidos",
            "Ciprofloxacino 500mg Comprimidos",
            "Ciprofloxacino 250mg Comprimidos",
            "Claritromicina 500mg Comprimidos",
            "Claritromicina 250mg/5ml Suspensao Oral 60ml",
            "Clonazepam 0,5mg Comprimidos",
            "Clonazepam 2mg Comprimidos",
            "Clonazepam 2,5mg/ml Gotas 20ml",
            "Clopidogrel 75mg Comprimidos",
            "Cloridrato de Tiamina 300mg Comprimidos",
            "Cloridrato de Sertralina 50mg Comprimidos",
            "Cloridrato de Sertralina 100mg Comprimidos",
            "Cloridrato de Venlafaxina 75mg Capsulas",
            "Cloridrato de Venlafaxina 150mg Capsulas",
            "Cloridrato de Fluoxetina 20mg Capsulas",
            "Cloridrato de Ciprofloxacino Colirio 5ml",
            "Cloreto de Potassio 600mg Drageas",
            "Complexo B Comprimidos",
            "Complexo B Solucao Oral 100ml",
            "Complexo B Gotas 20ml",
            "Complexo B Xarope Infantil 120ml",
            "Dexametasona 4mg Comprimidos",
            "Dexametasona Creme 10g",
            "Dexametasona Elixir 0,1mg/ml 120ml",
            "Dexclorfeniramina 2mg Comprimidos",
            "Dexclorfeniramina 0,4mg/ml Xarope 120ml",
            "Diazepam 5mg Comprimidos",
            "Diazepam 10mg Comprimidos",
            "Diclofenaco Potassico 50mg Comprimidos",
            "Diclofenaco Sodico 50mg Comprimidos",
            "Diclofenaco Dietilamonio Gel 60g",
            "Digesan Antiacido Liquido 240ml",
            "Dimenidrinato 50mg Comprimidos",
            "Dimenidrinato + Piridoxina Gotas 20ml",
            "Dipirona Sodica 500mg Comprimidos",
            "Dipirona Sodica 1g Comprimidos",
            "Dipirona Sodica 500mg/ml Gotas 20ml",
            "Dipirona Sodica 50mg/ml Solucao Oral Infantil 100ml",
            "Diosmina + Hesperidina 450mg + 50mg Comprimidos",
            "Domperidona 10mg Comprimidos",
            "Domperidona 1mg/ml Suspensao Oral 100ml",
            "Doxiciclina 100mg Capsulas",
            "Enalapril 5mg Comprimidos",
            "Enalapril 10mg Comprimidos",
            "Enalapril 20mg Comprimidos",
            "Escitalopram 10mg Comprimidos",
            "Escitalopram 20mg Comprimidos",
            "Esomeprazol 20mg Capsulas",
            "Esomeprazol 40mg Capsulas",
            "Espironolactona 25mg Comprimidos",
            "Espironolactona 50mg Comprimidos",
            "Fenitoina 100mg Comprimidos",
            "Fenobarbital 100mg Comprimidos",
            "Fexofenadina 120mg Comprimidos",
            "Fexofenadina 180mg Comprimidos",
            "Fexofenadina 6mg/ml Suspensao Oral 60ml",
            "Fluconazol 150mg Capsulas",
            "Fluconazol 200mg Capsulas",
            "Furosemida 40mg Comprimidos",
            "Furosemida 20mg Comprimidos",
            "Glibenclamida 5mg Comprimidos",
            "Gliclazida 30mg Comprimidos de Liberacao Prolongada",
            "Gliclazida 60mg Comprimidos de Liberacao Prolongada",
            "Hidralazina 25mg Comprimidos",
            "Hidralazina 50mg Comprimidos",
            "Hidroclorotiazida 25mg Comprimidos",
            "Hidroclorotiazida 50mg Comprimidos",
            "Hidroxizina 25mg Comprimidos",
            "Hidroxizina 2mg/ml Xarope 120ml",
            "Ibuprofeno 400mg Comprimidos",
            "Ibuprofeno 600mg Comprimidos",
            "Ibuprofeno 50mg/ml Suspensao Oral 30ml",
            "Ibuprofeno 100mg/ml Gotas Infantil 20ml",
            "Imecap Hair Comprimidos",
            "Isossorbida 10mg Comprimidos Sublinguais",
            "Isossorbida 20mg Comprimidos",
            "Itraconazol 100mg Capsulas",
            "Ivermectina 6mg Comprimidos",
            "Ivermectina 3mg Comprimidos",
            "Lactulona 667mg/ml Xarope 120ml",
            "Lactulona 667mg/ml Xarope 200ml",
            "Levofloxacino 500mg Comprimidos",
            "Levofloxacino 750mg Comprimidos",
            "Levotiroxina Sodica 25mcg Comprimidos",
            "Levotiroxina Sodica 50mcg Comprimidos",
            "Levotiroxina Sodica 75mcg Comprimidos",
            "Levotiroxina Sodica 100mcg Comprimidos",
            "Loperamida 2mg Capsulas",
            "Loratadina 10mg Comprimidos",
            "Loratadina 1mg/ml Xarope 100ml",
            "Loratadina 1mg/ml Xarope Infantil 100ml",
            "Losartana Potassica 25mg Comprimidos",
            "Losartana Potassica 50mg Comprimidos",
            "Losartana Potassica 100mg Comprimidos",
            "Maleato de Enalapril 20mg Comprimidos",
            "Meloxicam 7,5mg Comprimidos",
            "Meloxicam 15mg Comprimidos",
            "Metformina 500mg Comprimidos",
            "Metformina 850mg Comprimidos",
            "Metformina 1g Comprimidos",
            "Metildopa 250mg Comprimidos",
            "Metildopa 500mg Comprimidos",
            "Metoclopramida 10mg Comprimidos",
            "Metoclopramida 4mg/ml Gotas 10ml",
            "Metoprolol Succinato 25mg Comprimidos",
            "Metoprolol Succinato 50mg Comprimidos",
            "Metoprolol Succinato 100mg Comprimidos",
            "Metronidazol 250mg Comprimidos",
            "Metronidazol 400mg Comprimidos",
            "Metronidazol Suspensao Oral 40mg/ml 80ml",
            "Miconazol Creme Vaginal 80g",
            "Miconazol Creme Dermatologico 28g",
            "Montelucaste de Sodio 4mg Sache",
            "Montelucaste de Sodio 5mg Comprimidos Mastigaveis",
            "Montelucaste de Sodio 10mg Comprimidos",
            "Naproxeno Sodico 275mg Comprimidos",
            "Naproxeno Sodico 550mg Comprimidos",
            "Neomicina + Bacitracina Pomada 10g",
            "Nifedipino 10mg Capsulas",
            "Nifedipino 20mg Comprimidos",
            "Nimesulida 100mg Comprimidos",
            "Nimesulida 50mg/ml Gotas 15ml",
            "Nimesulida 50mg/ml Suspensao Oral 60ml",
            "Nistatina Suspensao Oral 100.000UI/ml 50ml",
            "Nistatina Creme Vaginal 60g",
            "Norfloxacino 400mg Comprimidos",
            "Omeprazol 20mg Capsulas",
            "Omeprazol 40mg Capsulas",
            "Ondansetrona 4mg Comprimidos Orodispersiveis",
            "Ondansetrona 8mg Comprimidos Orodispersiveis",
            "Oxibutinina 5mg Comprimidos",
            "Pantoprazol 20mg Comprimidos",
            "Pantoprazol 40mg Comprimidos",
            "Paracetamol 500mg Comprimidos",
            "Paracetamol 750mg Comprimidos",
            "Paracetamol 200mg/ml Gotas 15ml",
            "Paracetamol 32mg/ml Solucao Oral Infantil 60ml",
            "Permetrina Locao 1% 60ml",
            "Permetrina Locao 5% 60ml",
            "Prednisona 5mg Comprimidos",
            "Prednisona 20mg Comprimidos",
            "Prednisolona 3mg/ml Solucao Oral 60ml",
            "Prednisolona 11mg/ml Solucao Oral 60ml",
            "Pregabalina 75mg Capsulas",
            "Pregabalina 150mg Capsulas",
            "Propranolol 40mg Comprimidos",
            "Propranolol 80mg Comprimidos",
            "Quetiapina 25mg Comprimidos",
            "Quetiapina 100mg Comprimidos",
            "Quetiapina 200mg Comprimidos",
            "Ranitidina 150mg Comprimidos",
            "Ranitidina 300mg Comprimidos",
            "Risperidona 1mg Comprimidos",
            "Risperidona 2mg Comprimidos",
            "Risperidona Solucao Oral 1mg/ml 30ml",
            "Rosuvastatina 10mg Comprimidos",
            "Rosuvastatina 20mg Comprimidos",
            "Salbutamol 100mcg Aerossol 200 Doses",
            "Salbutamol Xarope 0,4mg/ml 120ml",
            "Secnidazol 1000mg Comprimidos",
            "Sinvastatina 10mg Comprimidos",
            "Sinvastatina 20mg Comprimidos",
            "Sinvastatina 40mg Comprimidos",
            "Soro Fisiologico 0,9% 100ml",
            "Soro Fisiologico 0,9% 250ml",
            "Soro Fisiologico 0,9% 500ml",
            "Soro Fisiologico 0,9% Spray Nasal 50ml",
            "Sulfametoxazol + Trimetoprima 400mg + 80mg Comprimidos",
            "Sulfametoxazol + Trimetoprima 800mg + 160mg Comprimidos",
            "Sulfametoxazol + Trimetoprima 40mg/ml + 8mg/ml Suspensao 100ml",
            "Tadalafila 5mg Comprimidos",
            "Tadalafila 20mg Comprimidos",
            "Tansulosina 0,4mg Capsulas",
            "Tiamazol 10mg Comprimidos",
            "Tiamina 300mg Comprimidos",
            "Tiocolchicosideo 4mg Comprimidos",
            "Tiocolchicosideo 8mg Comprimidos",
            "Topiramato 25mg Comprimidos",
            "Topiramato 50mg Comprimidos",
            "Topiramato 100mg Comprimidos",
            "Tramadol 50mg Capsulas",
            "Tramadol 100mg Capsulas",
            "Varfarina Sodica 5mg Comprimidos",
            "Vitamina C 1g Efervescente",
            "Vitamina C 500mg Comprimidos Mastigaveis",
            "Vitamina C 200mg/ml Gotas 20ml",
            "Vitamina C 200mg/ml Solucao Oral Infantil 20ml",
            "Vitamina D 1000UI Capsulas",
            "Vitamina D 2000UI Capsulas",
            "Vitamina D 7000UI Capsulas",
            "Vitamina D 200UI/gota Solucao Oral 20ml",
            "Acebrofilina 5mg/ml Xarope Adulto 120ml",
            "Acebrofilina 10mg/ml Xarope Pediatrico 120ml",
            "Acetilcisteina 20mg/ml Xarope 120ml",
            "Acetilcisteina 40mg/ml Xarope 120ml",
            "Acetilcisteina 600mg Envelopes",
            "Ambroxol 15mg/5ml Xarope Infantil 120ml",
            "Ambroxol 30mg/5ml Xarope Adulto 120ml",
            "Aminofilina 100mg Comprimidos",
            "Amiodarona 200mg Comprimidos",
            "Anlodipino + Losartana 5mg + 50mg Comprimidos",
            "Atorvastatina 10mg Comprimidos",
            "Atorvastatina 20mg Comprimidos",
            "Atorvastatina 40mg Comprimidos",
            "Azelastina Spray Nasal 140mcg",
            "Benzidamina Spray Oral 30ml",
            "Betametasona Creme 30g",
            "Betametasona Solucao Gotas 20ml",
            "Bisoprolol 2,5mg Comprimidos",
            "Bisoprolol 5mg Comprimidos",
            "Bisoprolol 10mg Comprimidos",
            "Brimonidina Colirio 5ml",
            "Brinzolamida Colirio 5ml",
            "Candesartana 8mg Comprimidos",
            "Candesartana 16mg Comprimidos",
            "Candesartana 32mg Comprimidos",
            "Carbocisteina 20mg/ml Xarope Infantil 100ml",
            "Carbocisteina 50mg/ml Xarope Adulto 100ml",
            "Cilostazol 50mg Comprimidos",
            "Cilostazol 100mg Comprimidos",
            "Clindamicina 300mg Capsulas",
            "Clindamicina Gel Dermatologico 30g",
            "Cloridrato de Duloxetina 30mg Capsulas",
            "Cloridrato de Duloxetina 60mg Capsulas",
            "Cloridrato de Donepezila 5mg Comprimidos",
            "Cloridrato de Donepezila 10mg Comprimidos",
            "Cloridrato de Memantina 10mg Comprimidos",
            "Cloridrato de Trazodona 50mg Comprimidos",
            "Cloridrato de Trazodona 150mg Comprimidos",
            "Colagenase Pomada 30g",
            "Dapagliflozina 10mg Comprimidos",
            "Desloratadina 5mg Comprimidos",
            "Desloratadina 0,5mg/ml Xarope 60ml",
            "Dorzolamida Colirio 5ml",
            "Empagliflozina 10mg Comprimidos",
            "Empagliflozina 25mg Comprimidos",
            "Ezetimiba 10mg Comprimidos",
            "Finasterida 1mg Comprimidos",
            "Finasterida 5mg Comprimidos",
            "Guaifenesina 100mg/15ml Xarope 120ml",
            "Guaifenesina 200mg/15ml Xarope 120ml",
            "Lansoprazol 30mg Capsulas",
            "Levodopa + Benserazida 100mg + 25mg Comprimidos",
            "Levodopa + Benserazida 200mg + 50mg Comprimidos",
            "Levodopa + Carbidopa 250mg + 25mg Comprimidos",
            "Linagliptina 5mg Comprimidos",
            "Mebendazol 100mg Comprimidos",
            "Mebendazol Suspensao Oral 20mg/ml 30ml",
            "Mesalazina 400mg Comprimidos",
            "Mesalazina 800mg Comprimidos",
            "Mupirocina Pomada 15g",
            "Nebivolol 5mg Comprimidos",
            "Nitazoxanida 500mg Comprimidos",
            "Nitazoxanida 20mg/ml Suspensao 45ml",
            "Olmesartana Medoxomila 20mg Comprimidos",
            "Olmesartana Medoxomila 40mg Comprimidos",
            "Olopatadina Colirio 2,5ml",
            "Oximetazolina Solucao Nasal 30ml",
            "Pimecrolimo Creme 10g",
            "Polivitaminico A-Z Comprimidos",
            "Prometazina 25mg Comprimidos",
            "Prometazina Xarope 120ml",
            "Ramipril 2,5mg Capsulas",
            "Ramipril 5mg Capsulas",
            "Ramipril 10mg Capsulas",
            "Rivaroxabana 10mg Comprimidos",
            "Rivaroxabana 15mg Comprimidos",
            "Rivaroxabana 20mg Comprimidos",
            "Saccharomyces boulardii 100mg Capsulas",
            "Saccharomyces boulardii 200mg Envelopes",
            "Saxagliptina 5mg Comprimidos",
            "Sitagliptina 50mg Comprimidos",
            "Sitagliptina 100mg Comprimidos",
            "Sucralfato 1g Comprimidos",
            "Sucralfato Suspensao 200ml",
            "Sumatriptana 50mg Comprimidos",
            "Timolol Colirio 5ml",
            "Travoprosta Colirio 2,5ml",
            "Trometamol Cetorolaco 10mg Comprimidos",
            "Valsartana 80mg Comprimidos",
            "Valsartana 160mg Comprimidos",
            "Valsartana 320mg Comprimidos",
            "Vildagliptina 50mg Comprimidos",
            "Vildagliptina + Metformina 50mg + 850mg Comprimidos",
            "Vildagliptina + Metformina 50mg + 1000mg Comprimidos",
            "Bimatoprosta Solucao Oftalmica 3ml",
            "Cetoprofeno 100mg Comprimidos",
            "Cetoprofeno 20mg/ml Gotas 20ml",
            "Cetoprofeno Gel 30g",
            "Dexlansoprazol 30mg Capsulas",
            "Dexlansoprazol 60mg Capsulas",
            "Glimepirida 2mg Comprimidos",
            "Glimepirida 4mg Comprimidos",
            "Lercanidipino 10mg Comprimidos",
            "Lercanidipino 20mg Comprimidos",
            "Rupatadina 10mg Comprimidos",
            "Rupatadina Solucao Oral 100ml",
            "Mometasona Creme 20g",
            "Mometasona Solucao Capilar 30ml",
            "Levocetirizina 5mg Comprimidos",
            "Levocetirizina Xarope 100ml",
            "Xarope Expectorante Guaco 120ml",
            "Xarope Propolis e Mel 120ml",
            "Diclofenaco Resinato Gotas 20ml",
            "Etoricoxibe 60mg Comprimidos",
            "Etoricoxibe 90mg Comprimidos",
            "Etoricoxibe 120mg Comprimidos",
            "Amoxicilina 500mg Capsulas Original",
            "Losartana Potassica 50mg Comprimidos Original",
            "Omeprazol 20mg Capsulas Original",
            "Paracetamol 750mg Comprimidos Original",
            "Dipirona Sodica 1g Comprimidos Original",
            "Ibuprofeno 600mg Comprimidos Original",
            "Cetirizina 10mg Comprimidos Original",
            "Loratadina 10mg Comprimidos Original",
            "Nimesulida 100mg Comprimidos Original",
            "Prednisona 20mg Comprimidos Original",
            "Sabonete Barra Glicerina 90g",
            "Sabonete Barra Antibacteriano 85g",
            "Sabonete Liquido Erva Doce 250ml",
            "Sabonete Liquido Intimo 200ml",
            "Sabonete Liquido Facial 150ml",
            "Shampoo Hidratacao 325ml",
            "Shampoo Reparacao 325ml",
            "Shampoo Infantil 200ml",
            "Shampoo Matizador 300ml",
            "Condicionador Hidratacao 325ml",
            "Condicionador Reparacao 325ml",
            "Condicionador Cachos 300ml",
            "Mascara Capilar Nutricao 300g",
            "Mascara Capilar Reconstrucao 300g",
            "Creme Para Pentear Cachos 300ml",
            "Gel Fixador Capilar 240g",
            "Pomada Modeladora Capilar 70g",
            "Cera Modeladora Capilar 120g",
            "Escova de Cabelo Raquete",
            "Pente Fino Antipiolho",
            "Kit Manicure Basico",
            "Desodorante Aerosol Masculino 150ml",
            "Desodorante Aerosol Feminino 150ml",
            "Desodorante Roll-on Sem Aluminio 50ml",
            "Desodorante Creme 55g",
            "Colonia Infantil 100ml",
            "Agua Micelar 200ml",
            "Serum Facial Vitamina C 30ml",
            "Serum Facial Acido Hialuronico 30ml",
            "Hidratante Facial Oil Free 100g",
            "Creme Hidratante Corporal 200ml",
            "Creme Hidratante Maos 75g",
            "Locao Corporal Amendoas 200ml",
            "Locao Corporal Aveia 200ml",
            "Hidratante Pos-Sol 120g",
            "Protetor Solar Facial FPS 50 60g",
            "Protetor Solar Corporal FPS 30 120ml",
            "Protetor Solar Corporal FPS 50 120ml",
            "Protetor Labial FPS 30",
            "Protetor Labial Infantil FPS 15",
            "Repelente Aerosol 100ml",
            "Repelente Infantil Locao 100ml",
            "Gel Higienizante Maos 70% 500ml",
            "Lenco Umedecido Infantil 48 Unidades",
            "Lenco Umedecido Antibacteriano 20 Unidades",
            "Toalhas Umedecidas Demaquilantes 25 Unidades",
            "Papel Higienico Umedificado 50 Unidades",
            "Cotonete Flexivel 75 Unidades",
            "Algodao Bolas 100g",
            "Algodao Discos 50 Unidades",
            "Compressa de Gaze Esteril 13 Fios 10 Unidades",
            "Curativo Transparente 35 Unidades",
            "Curativo Redondo 20 Unidades",
            "Bandagem Elastica 10cm",
            "Bandagem Elastica 15cm",
            "Atadura Crepom 10cm 1,8m",
            "Atadura Crepom 15cm 1,8m",
            "Atadura Crepom 20cm 1,8m",
            "Esparadrapo Impermeavel 5cm x 4,5m",
            "Esparadrapo Microporoso 2,5cm x 4,5m",
            "Termometro Digital Flexivel",
            "Bolsa Termica Gel Pequena",
            "Cinta Termica Adesiva 2 Unidades",
            "Mascara Descartavel Tripla 10 Unidades",
            "Fralda Infantil Recem-Nascido 36 Unidades",
            "Fralda Infantil P 34 Unidades",
            "Fralda Infantil M 32 Unidades",
            "Fralda Infantil G 28 Unidades",
            "Fralda Infantil XG 24 Unidades",
            "Fralda Geriatrica M 8 Unidades",
            "Fralda Geriatrica G 8 Unidades",
            "Absorvente Noturno com Abas 8 Unidades",
            "Absorvente Diario 15 Unidades",
            "Protetor Diario Respiravel 40 Unidades",
            "Escova Dental Macia",
            "Escova Dental Media",
            "Escova Interdental 6 Unidades",
            "Fio Dental Menta 100m",
            "Fio Dental Extra Fino 50m",
            "Creme Dental Branqueador 90g",
            "Creme Dental Anticarie 90g",
            "Creme Dental Sensibilidade 90g",
            "Antisseptico Bucal Menta 250ml",
            "Antisseptico Bucal Zero Alcool 250ml",
            "Gel Lubrificante Intimo 50g",
            "Sabonete Intimo Delicado 200ml",
            "Creme Depilatorio Corporal 120g",
            "Cera Depilatoria Fria 16 Folhas",
            "Esponja de Banho Suave",
            "Bucha Vegetal Corporal",
            "Lixa de Unha 6 Unidades",
            "Acetona Cosmetica 100ml",
            "Removedor de Esmalte Sem Acetona 100ml",
            "Esmalte Tratamento Fortalecedor 8ml",
            "Talco Antisseptico Pes 100g",
            "Spray Corporal Refrescante 100ml",
            "Sais de Banho Relaxante 300g",
            "Po Compacto Facial 10g",
            "Base Liquida Facial 30ml",
            "Vaselina Liquida 100ml",
            "Tiras Removedoras de Cravos 6 Unidades",
            "Touca de Banho 1 Unidade",
            "Aparelho de Barbear 2 Laminas 2 Unidades",
            "Aparelho de Barbear 3 Laminas 2 Unidades",
            "Abacavir 300mg Comprimidos",
            "Albocresil Solucao 12ml",
            "Alginato de Sodio Suspensao 150ml",
            "Alisquireno 150mg Comprimidos",
            "Alisquireno 300mg Comprimidos",
            "Amantadina 100mg Capsulas",
            "Amlodipino + Atenolol 5mg + 25mg Comprimidos",
            "Amlodipino + Valsartana 5mg + 160mg Comprimidos",
            "Arginina Aspartato Envelopes",
            "Arnica Gel Massageador 60g",
            "Baclofeno 10mg Comprimidos",
            "Baclofeno 20mg Comprimidos",
            "Beclometasona Spray Nasal 50mcg",
            "Benfotiamina 150mg Capsulas",
            "Benzydamina Pastilhas",
            "Betahistina 16mg Comprimidos",
            "Betahistina 24mg Comprimidos",
            "Bicarbonato de Sodio Envelopes",
            "Biotina 5mg Capsulas",
            "Boswellia Serrata Capsulas",
            "Brivaracetam 50mg Comprimidos",
            "Brivaracetam 100mg Comprimidos",
            "Cabergolina 0,5mg Comprimidos",
            "Calcitriol 0,25mcg Capsulas",
            "Calcitriol 0,5mcg Capsulas",
            "Calcio Citrato + Vitamina D Comprimidos",
            "Camomila Solucao Oral 30ml",
            "Canagliflozina 100mg Comprimidos",
            "Canagliflozina 300mg Comprimidos",
            "Carisoprodol 125mg Comprimidos",
            "Carisoprodol 250mg Comprimidos",
            "Cefadroxila 500mg Capsulas",
            "Cefadroxila Suspensao Oral 100ml",
            "Cefixima 400mg Comprimidos",
            "Cefixima Suspensao Oral 50ml",
            "Celecoxibe 100mg Capsulas",
            "Celecoxibe 200mg Capsulas",
            "Cinarizina + Dimenidrinato Comprimidos",
            "Ciprofibrato 100mg Comprimidos",
            "Citicolina 500mg Capsulas",
            "Citicolina 1000mg Ampolas Orais",
            "Clobetasol Creme 30g",
            "Clobetasol Shampoo 125ml",
            "Cloperastina Xarope 120ml",
            "Coenzima Q10 100mg Capsulas",
            "Colageno Hidrolisado Sache",
            "Colirio Lubrificante 10ml",
            "Colirio Lubrificante Sem Conservante 10ml",
            "Cumarina + Troxerrutina Comprimidos",
            "Dexpantenol Creme 30g",
            "Dexpantenol Solucao 50ml",
            "Dexslanoprazol 30mg Capsulas",
            "Diosmina 900mg Comprimidos",
            "Dipropionato de Beclometasona Inalador 200 Doses",
            "Divalproato de Sodio 250mg Comprimidos",
            "Divalproato de Sodio 500mg Comprimidos",
            "Dropropizina 1,5mg/ml Xarope Infantil 120ml",
            "Dropropizina 3mg/ml Xarope Adulto 120ml",
            "Edoxabana 30mg Comprimidos",
            "Edoxabana 60mg Comprimidos",
            "Enoxaparina 40mg Seringa",
            "Enoxaparina 60mg Seringa",
            "Eritromicina 500mg Comprimidos",
            "Eritromicina Suspensao Oral 60ml",
            "Escina Gel 40g",
            "Eslicarbazepina 400mg Comprimidos",
            "Eslicarbazepina 800mg Comprimidos",
            "Espiramicina 500mg Comprimidos",
            "Estradiol Gel 80g",
            "Estradiol Creme Vaginal 50g",
            "Famciclovir 250mg Comprimidos",
            "Famciclovir 500mg Comprimidos",
            "Fenazopiridina 100mg Comprimidos",
            "Fenazopiridina 200mg Comprimidos",
            "Ferro Quelato Capsulas",
            "Fexaramina Xarope 120ml",
            "Flecainida 100mg Comprimidos",
            "Fosinopril 10mg Comprimidos",
            "Fosinopril 20mg Comprimidos",
            "Ginkgo Biloba 80mg Comprimidos",
            "Ginkgo Biloba 120mg Comprimidos",
            "Glicosamina + Condroitina Sache",
            "Harpagophytum Procumbens Capsulas",
            "Heparinoide Gel 40g",
            "Ibandronato de Sodio 150mg Comprimidos",
            "Imunoglobulina Oral Sache",
            "Indapamida 1,5mg Comprimidos",
            "Indapamida 2,5mg Comprimidos",
            "Isotretinoina 10mg Capsulas",
            "Isotretinoina 20mg Capsulas",
            "Lacidipino 4mg Comprimidos",
            "Lamotrigina 25mg Comprimidos",
            "Lamotrigina 50mg Comprimidos",
            "Lamotrigina 100mg Comprimidos",
            "Lamotrigina 200mg Comprimidos",
            "Latanoprosta Colirio 2,5ml",
            "Levobupivacaina Solucao 20ml",
            "Levosulpirida 25mg Comprimidos",
            "Levosulpirida Gotas 20ml",
            "Lidocaina Spray 50ml",
            "Luteina + Zeaxantina Capsulas",
            "MagnÃ©sio Quelato Capsulas",
            "Melatonina 0,21mg Comprimidos",
            "Melatonina 3mg Gotas",
            "Mirabegrona 25mg Comprimidos",
            "Mirabegrona 50mg Comprimidos",
            "Modafinila 100mg Comprimidos",
            "Modafinila 200mg Comprimidos",
            "Monuril Fosfomicina Sache 3g",
            "N-Acetil Glucosamina Sache",
            "Naproxeno + Esomeprazol Comprimidos",
            "Nicotinamida Gel 30g",
            "Nifuroxazida 200mg Capsulas",
            "Nifuroxazida Suspensao 90ml",
            "Nortriptilina 25mg Capsulas",
            "Nortriptilina 50mg Capsulas",
            "Omega 3 Capsulas",
            "Orlistate 120mg Capsulas",
            "Oseltamivir 30mg Capsulas",
            "Oseltamivir 45mg Capsulas",
            "Oseltamivir 75mg Capsulas",
            "Oxcarbazepina 300mg Comprimidos",
            "Oxcarbazepina 600mg Comprimidos",
            "Paliperidona 3mg Comprimidos",
            "Paliperidona 6mg Comprimidos",
            "Periciazina 10mg Comprimidos",
            "Periciazina Solucao Oral 20ml",
            "Picosulfato de Sodio Gotas 30ml",
            "Picosulfato de Sodio Solucao Oral 120ml",
            "Piracetam 400mg Capsulas",
            "Piracetam 800mg Comprimidos",
            "Piridoxina 50mg Comprimidos",
            "Pramipexol 0,125mg Comprimidos",
            "Pramipexol 0,25mg Comprimidos",
            "Pramipexol 1mg Comprimidos",
            "ProbiÃ³tico Infantil Sache",
            "Propafenona 300mg Comprimidos",
            "Propatilnitrato 10mg Comprimidos",
            "Quercetina Capsulas",
            "Risedronato de Sodio 35mg Comprimidos",
            "Risedronato de Sodio 150mg Comprimidos",
            "Roxitromicina 150mg Comprimidos",
            "Roxitromicina 300mg Comprimidos",
            "Selegilina 5mg Comprimidos",
            "Sildenafila 25mg Comprimidos",
            "Sildenafila 50mg Comprimidos",
            "Sildenafila 100mg Comprimidos",
            "Silimarina 200mg Capsulas",
            "Silimarina 300mg Capsulas",
            "Sulfadiazina de Prata Creme 30g",
            "Taurina Capsulas",
            "Telmisartana 20mg Comprimidos",
            "Telmisartana 40mg Comprimidos",
            "Telmisartana 80mg Comprimidos",
            "Tenoxicam 20mg Comprimidos",
            "Terbinafina 250mg Comprimidos",
            "Tetraciclina Pomada Oftalmica 3,5g",
            "Tiamulina 50mg Comprimidos",
            "Tobramicina Colirio 5ml",
            "Tobramicina + Dexametasona Colirio 5ml",
            "Tolterodina 2mg Comprimidos",
            "Tolterodina 4mg Capsulas",
            "Trazodona 50mg Comprimidos Original",
            "Trazodona 150mg Comprimidos Original",
            "Triancinolona Pasta Oral 10g",
            "Ursodesoxicolico Acido 300mg Capsulas",
            "Venlafaxina 37,5mg Capsulas",
            "Venlafaxina 75mg Capsulas Original",
            "Venlafaxina 150mg Capsulas Original",
            "Verapamil 80mg Comprimidos",
            "Verapamil 120mg Comprimidos",
            "Vitamina K Gotas 20ml",
            "Xilitol Pastilhas",
            "Xilitol Spray Bucal 30ml",
            "Espuma de Barbear Sensitive 200ml",
            "Gel Dental Infantil Tutti Frutti 50g",
            "Gel Dental Carvao Ativado 90g",
            "Limpador de Pele Facial 120ml",
            "Sabonete Esfoliante Facial 100g",
            "Sabonete em Barra Carvao 90g",
            "Sabonete em Barra Hidratante 90g",
            "Hidratante Labial Colorido",
            "Fixador de Dentadura Creme 40g",
            "Escova Dental Infantil Extra Macia",
            "Escova Dental Ortodontica",
            "Enxaguante Bucal Infantil 250ml",
            "Enxaguante Bucal Gengiva 250ml",
            "Creme Dental Herbal 90g",
            "Creme Dental Infantil Morango 50g",
            "Creme para Assadura 45g",
            "Pomada para Assadura 90g",
            "Talco Liquido Infantil 120ml",
            "Colonia Infantil Lavanda 100ml",
            "Shampoo Anticaspa 200ml",
            "Shampoo Antiqueda 200ml",
            "Condicionador Antiqueda 200ml",
            "Ampola Capilar Reconstrutora 15ml",
            "Ampola Capilar Hidratacao 15ml",
            "Oleo Capilar Reparador 60ml",
            "Creme para Maos Ureia 60g",
            "Creme para Pes Ureia 80g",
            "Esfoliante Corporal 200g",
            "Esfoliante Facial 100g",
            "Mascara Facial Argila Verde 100g",
            "Mascara Facial Argila Rosa 100g",
            "Agua Termal 150ml",
            "Demaquilante Bifasico 120ml",
            "Espuma de Limpeza Facial 150ml",
            "Serum Niacinamida 30ml",
            "Serum Retinol 30ml",
            "Hidratante Corporal Infantil 200ml",
            "Hidratante Corporal Sem Perfume 200ml",
            "Oleo Corporal Amendoas 100ml",
            "Oleo Corporal Semente de Uva 100ml",
            "Desodorante em Stick 45g",
            "Desodorante Clinical 96h 150ml",
            "Sabonete Intimo Calmante 200ml",
            "Protetor Diario Sem Perfume 40 Unidades",
            "Absorvente Cobertura Seca 8 Unidades",
            "Algodao Hidrofilo 50g",
            "Gaze Nao Esteril 100 Unidades",
            "Curativo Infantil Decorado 20 Unidades",
            "Curativo Antibolhas 6 Unidades",
            "Bandagem Adesiva Elastico 5cm",
            "Esparadrapo Transparente 10cm x 4,5m",
            "Micropore Bege 5cm x 4,5m",
            "Luva de Procedimento M 10 Unidades",
            "Luva de Procedimento G 10 Unidades",
            "Mascara PFF2 2 Unidades",
            "Mascara de Dormir Anatomica",
            "Palmilha Gel Conforto Par",
            "Protetor Calcanhar Silicone Par",
            "Soro de Limpeza Nasal 100ml",
            "Lavador Nasal 240ml",
            "Escova Massageadora Couro Cabeludo",
            "Organizador de Medicamentos Semanal",
            "Caixa Organizadora de Comprimidos",
            "Copo Dosador Graduado 20ml",
            "Colher Dosadora 10ml",
            "Mamadeira Anticolica 150ml",
            "Mamadeira Anticolica 250ml",
            "Bico de Mamadeira Silicone 2 Unidades",
            "Chupeta Ortodontica 0-6 Meses",
            "Chupeta Ortodontica 6-18 Meses",
            "Prendedor de Chupeta",
            "Toalha Umedecida Recem Nascido 96 Unidades",
            "Fralda Geriatrica XG 7 Unidades",
            "Assento Elevatorio Macio",
            "Almofada Cervical Gel"
    };

    public static Connection getConexao() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conexao = DriverManager.getConnection(URL);
            inicializarBanco(conexao);
            return conexao;
        } catch (Exception e) {
            System.err.println("Erro na conexao: " + e.getMessage());
            return null;
        }
    }

    private static synchronized void inicializarBanco(Connection conexao) throws SQLException {
        if (bancoInicializado || conexao == null) {
            return;
        }

        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuarios (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        login TEXT NOT NULL UNIQUE,
                        senha TEXT NOT NULL,
                        cargo TEXT NOT NULL
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS clientes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        cpf TEXT,
                        telefone TEXT
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS produtos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        codigo_barras TEXT,
                        quantidade_estoque INTEGER NOT NULL DEFAULT 0,
                        data_validade TEXT,
                        preco REAL NOT NULL DEFAULT 0,
                        preco_custo REAL NOT NULL DEFAULT 0,
                        categoria TEXT,
                        laboratorio TEXT,
                        tipo_controle TEXT,
                        estoque_minimo INTEGER NOT NULL DEFAULT 0,
                        classe_comercial TEXT
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS vendas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        usuario_id INTEGER,
                        usuario_nome TEXT,
                        cliente_id INTEGER,
                        cliente_nome TEXT,
                        cliente_cpf TEXT,
                        venda_sem_cadastro INTEGER NOT NULL DEFAULT 0,
                        itens_resumo TEXT,
                        quantidade_itens INTEGER NOT NULL DEFAULT 0,
                        total REAL NOT NULL,
                        desconto_total REAL NOT NULL DEFAULT 0,
                        comissao REAL NOT NULL DEFAULT 0,
                        data_venda TEXT NOT NULL
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS configuracoes (
                        chave TEXT PRIMARY KEY,
                        valor TEXT NOT NULL
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS comandas_caixa (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        comanda TEXT NOT NULL,
                        usuario_id INTEGER NOT NULL,
                        usuario_nome TEXT NOT NULL,
                        cliente_id INTEGER,
                        cliente_nome TEXT,
                        cliente_cpf TEXT,
                        venda_sem_cadastro INTEGER NOT NULL DEFAULT 0,
                        itens_resumo TEXT,
                        itens_detalhe TEXT,
                        quantidade_itens INTEGER NOT NULL DEFAULT 0,
                        total REAL NOT NULL DEFAULT 0,
                        desconto_total REAL NOT NULL DEFAULT 0,
                        comissao REAL NOT NULL DEFAULT 0,
                        tipo_venda TEXT NOT NULL DEFAULT 'Balcao',
                        status TEXT NOT NULL DEFAULT 'Aberta',
                        data_abertura TEXT NOT NULL,
                        endereco TEXT,
                        bairro TEXT,
                        cidade TEXT,
                        taxa_entrega REAL NOT NULL DEFAULT 0,
                        precisa_troco_entrega INTEGER NOT NULL DEFAULT 0,
                        troco_para REAL NOT NULL DEFAULT 0
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS fornecedores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        cnpj TEXT NOT NULL UNIQUE
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS medicos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        tipo_registro TEXT NOT NULL,
                        numero_registro TEXT NOT NULL,
                        uf_registro TEXT
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS recebimentos_estoque (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        fornecedor_id INTEGER,
                        fornecedor_nome TEXT,
                        fornecedor_cnpj TEXT,
                        chave_nfe TEXT,
                        numero_nota TEXT,
                        xml_origem TEXT,
                        quantidade_itens INTEGER NOT NULL DEFAULT 0,
                        valor_total REAL NOT NULL DEFAULT 0,
                        data_recebimento TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS recebimentos_estoque_itens (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        recebimento_id INTEGER NOT NULL,
                        produto_id INTEGER,
                        produto_nota TEXT NOT NULL,
                        codigo_nota TEXT,
                        quantidade INTEGER NOT NULL DEFAULT 0,
                        valor_unitario REAL NOT NULL DEFAULT 0,
                        lote TEXT,
                        data_validade TEXT,
                        laboratorio_nota TEXT,
                        custo_anterior REAL NOT NULL DEFAULT 0,
                        custo_novo REAL NOT NULL DEFAULT 0,
                        houve_alteracao_custo INTEGER NOT NULL DEFAULT 0
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS lotes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        produto_id INTEGER NOT NULL,
                        numero_lote TEXT NOT NULL,
                        quantidade_atual INTEGER NOT NULL DEFAULT 0,
                        data_validade TEXT,
                        laboratorio TEXT,
                        preco_custo REAL NOT NULL DEFAULT 0
                    )
                    """);

            stmt.executeUpdate("""
                    CREATE UNIQUE INDEX IF NOT EXISTS idx_lotes_produto_numero
                    ON lotes (produto_id, numero_lote)
                    """);
        }

        garantirColunasVendas(conexao);
        garantirColunasComandas(conexao);
        garantirColunasProdutos(conexao);
        garantirColunasRecebimentos(conexao);
        garantirColunasMedicos(conexao);
        garantirUsuariosPadrao(conexao);
        garantirFornecedoresPadrao(conexao);
        sincronizarCatalogoProdutos(conexao);
        sincronizarTipoControleProdutos(conexao);
        bancoInicializado = true;
    }

    private static void garantirColunasVendas(Connection conexao) {
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN usuario_id INTEGER");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN usuario_nome TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN cliente_id INTEGER");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN cliente_nome TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN cliente_cpf TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN venda_sem_cadastro INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN itens_resumo TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN quantidade_itens INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN desconto_total REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN comissao REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN tipo_venda TEXT NOT NULL DEFAULT 'Balcao'");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN comanda TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN forma_pagamento TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN valor_recebido REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE vendas ADD COLUMN troco REAL NOT NULL DEFAULT 0");
        try (PreparedStatement stmt = conexao.prepareStatement(
                "UPDATE vendas SET tipo_venda = 'Balcao' WHERE tipo_venda IS NULL OR TRIM(tipo_venda) = ''")) {
            stmt.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private static void garantirColunasComandas(Connection conexao) {
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN usuario_id INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN usuario_nome TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN cliente_id INTEGER");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN cliente_nome TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN cliente_cpf TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN venda_sem_cadastro INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN itens_resumo TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN itens_detalhe TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN quantidade_itens INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN total REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN desconto_total REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN comissao REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN tipo_venda TEXT NOT NULL DEFAULT 'Balcao'");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN status TEXT NOT NULL DEFAULT 'Aberta'");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN data_abertura TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN endereco TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN bairro TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN cidade TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN taxa_entrega REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN precisa_troco_entrega INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE comandas_caixa ADD COLUMN troco_para REAL NOT NULL DEFAULT 0");
    }

    private static void adicionarColunaSeNecessario(Connection conexao, String sql) {
        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException ignored) {
        }
    }

    private static void garantirColunasProdutos(Connection conexao) {
        adicionarColunaSeNecessario(conexao, "ALTER TABLE produtos ADD COLUMN categoria TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE produtos ADD COLUMN laboratorio TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE produtos ADD COLUMN tipo_controle TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE produtos ADD COLUMN estoque_minimo INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE produtos ADD COLUMN classe_comercial TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE produtos ADD COLUMN preco_custo REAL NOT NULL DEFAULT 0");
    }

    private static void garantirColunasRecebimentos(Connection conexao) {
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN fornecedor_id INTEGER");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN fornecedor_nome TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN fornecedor_cnpj TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN chave_nfe TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN numero_nota TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN xml_origem TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN quantidade_itens INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN valor_total REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque ADD COLUMN data_recebimento TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN produto_id INTEGER");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN produto_nota TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN codigo_nota TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN quantidade INTEGER NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN valor_unitario REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN lote TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN data_validade TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN laboratorio_nota TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN custo_anterior REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN custo_novo REAL NOT NULL DEFAULT 0");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE recebimentos_estoque_itens ADD COLUMN houve_alteracao_custo INTEGER NOT NULL DEFAULT 0");
    }

    private static void garantirColunasMedicos(Connection conexao) {
        adicionarColunaSeNecessario(conexao, "ALTER TABLE medicos ADD COLUMN nome TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE medicos ADD COLUMN tipo_registro TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE medicos ADD COLUMN numero_registro TEXT");
        adicionarColunaSeNecessario(conexao, "ALTER TABLE medicos ADD COLUMN uf_registro TEXT");
    }

    private static void garantirUsuariosPadrao(Connection conexao) throws SQLException {
        inserirUsuarioPadrao(conexao, "Administrador Padrao", "admin", "admin", "Administrador");
        inserirUsuarioPadrao(conexao, "Chefe Padrao", "chefe", "12345", "Chefe");
        inserirUsuarioPadrao(conexao, "Atendente Julia", "atendente1", "12345", "Atendente");
        inserirUsuarioPadrao(conexao, "Atendente Marcos", "atendente2", "12345", "Atendente");
    }

    private static void garantirFornecedoresPadrao(Connection conexao) throws SQLException {
        inserirFornecedorPadrao(conexao, "Santa Cruz", FORNECEDOR_SANTA_CRUZ_CNPJ);
    }

    private static void inserirUsuarioPadrao(Connection conexao, String nome, String login, String senha, String cargo)
            throws SQLException {
        String sqlBusca = "SELECT id FROM usuarios WHERE login = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlBusca)) {
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String sqlInsercao = "INSERT INTO usuarios (nome, login, senha, cargo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlInsercao)) {
            stmt.setString(1, nome);
            stmt.setString(2, login);
            stmt.setString(3, senha);
            stmt.setString(4, cargo);
            stmt.executeUpdate();
        }
    }

    private static void inserirFornecedorPadrao(Connection conexao, String nome, String cnpj) throws SQLException {
        String sqlBusca = "SELECT id FROM fornecedores WHERE cnpj = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlBusca)) {
            stmt.setString(1, cnpj);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String sqlInsercao = "INSERT INTO fornecedores (nome, cnpj) VALUES (?, ?)";
        try (PreparedStatement stmt = conexao.prepareStatement(sqlInsercao)) {
            stmt.setString(1, nome);
            stmt.setString(2, cnpj);
            stmt.executeUpdate();
        }
    }

    private static void sincronizarCatalogoProdutos(Connection conexao) throws SQLException {
        String versaoAtual = buscarConfiguracao(conexao, "versao_catalogo_produtos");
        if (VERSAO_CATALOGO.equals(versaoAtual)) {
            return;
        }

        try (Statement stmt = conexao.createStatement()) {
            stmt.executeUpdate("DELETE FROM produtos");
            stmt.executeUpdate("DELETE FROM sqlite_sequence WHERE name = 'produtos'");
        }

        long codigoBase = 7892000101000L;
        for (int i = 0; i < CATALOGO_PRODUTOS.length; i++) {
            int estoque = 18 + (i % 55);
            double preco = 8.50 + ((i % 17) * 2.15);
            String validade = (i % 3 == 0) ? "2027-12-31" : (i % 3 == 1 ? "2028-06-30" : "2028-11-30");
            inserirProduto(
                    conexao,
                    CATALOGO_PRODUTOS[i],
                    String.valueOf(codigoBase + i),
                    estoque,
                    validade,
                    preco,
                    inferirCategoria(CATALOGO_PRODUTOS[i]),
                    inferirLaboratorio(i),
                    inferirTipoControle(CATALOGO_PRODUTOS[i]),
                    inferirEstoqueMinimo(CATALOGO_PRODUTOS[i], i),
                    inferirClasseComercial(CATALOGO_PRODUTOS[i], i)
            );
        }

        salvarConfiguracao(conexao, "versao_catalogo_produtos", VERSAO_CATALOGO);
    }

    private static String buscarConfiguracao(Connection conexao, String chave) throws SQLException {
        String sql = "SELECT valor FROM configuracoes WHERE chave = ?";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("valor") : null;
            }
        }
    }

    private static void salvarConfiguracao(Connection conexao, String chave, String valor) throws SQLException {
        String sql = """
                INSERT INTO configuracoes (chave, valor) VALUES (?, ?)
                ON CONFLICT(chave) DO UPDATE SET valor = excluded.valor
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, chave);
            stmt.setString(2, valor);
            stmt.executeUpdate();
        }
    }

    private static void inserirProduto(Connection conexao, String nome, String codigoBarras, int estoque,
                                       String validade, double preco, String categoria, String laboratorio,
                                       String tipoControle, int estoqueMinimo, String classeComercial) throws SQLException {
        String sql = """
                INSERT INTO produtos (nome, codigo_barras, quantidade_estoque, data_validade, preco, preco_custo,
                                      categoria, laboratorio, tipo_controle, estoque_minimo, classe_comercial)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            double precoFinal = ajustarPrecoPorClasse(preco, classeComercial);
            double precoCusto = Math.max(0.01, preco * 0.62);
            stmt.setString(1, nome);
            stmt.setString(2, codigoBarras);
            stmt.setInt(3, estoque);
            stmt.setString(4, validade);
            stmt.setDouble(5, precoFinal);
            stmt.setDouble(6, precoCusto);
            stmt.setString(7, categoria);
            stmt.setString(8, laboratorio);
            stmt.setString(9, tipoControle);
            stmt.setInt(10, estoqueMinimo);
            stmt.setString(11, classeComercial);
            stmt.executeUpdate();
        }
    }

    private static double ajustarPrecoPorClasse(double precoBase, String classeComercial) {
        if ("Original".equalsIgnoreCase(classeComercial)) {
            return precoBase * 1.24;
        }
        if ("Perfumaria".equalsIgnoreCase(classeComercial)) {
            return precoBase * 1.18;
        }
        if ("Similar".equalsIgnoreCase(classeComercial)) {
            return precoBase * 1.08;
        }
        return precoBase;
    }

    private static String inferirCategoria(String nome) {
        String item = nome.toLowerCase();
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("ciprofloxacino") || item.contains("claritromicina")
                || item.contains("levofloxacino") || item.contains("metronidazol")
                || item.contains("norfloxacino") || item.contains("sulfametoxazol")) {
            return "Antibioticos";
        }
        if (item.contains("loratadina") || item.contains("cetirizina") || item.contains("dexclorfeniramina")
                || item.contains("fexofenadina") || item.contains("hidroxizina") || item.contains("montelucaste")) {
            return "Antialergicos";
        }
        if (item.contains("dipirona") || item.contains("paracetamol") || item.contains("ibuprofeno")
                || item.contains("nimesulida") || item.contains("naproxeno") || item.contains("diclofenaco")
                || item.contains("meloxicam") || item.contains("tramadol")) {
            return "Analgesicos e Anti-inflamatorios";
        }
        if (item.contains("losartana") || item.contains("atenolol") || item.contains("enalapril")
                || item.contains("carvedilol") || item.contains("captopril") || item.contains("furosemida")
                || item.contains("hidroclorotiazida") || item.contains("anlodipino")
                || item.contains("metoprolol") || item.contains("nifedipino")) {
            return "Cardiovascular";
        }
        if (item.contains("omeprazol") || item.contains("esomeprazol") || item.contains("pantoprazol")
                || item.contains("antiacido") || item.contains("lactulona") || item.contains("domperidona")
                || item.contains("metoclopramida") || item.contains("ondansetrona") || item.contains("loperamida")) {
            return "Gastrointestinal";
        }
        if (item.contains("metformina") || item.contains("glibenclamida") || item.contains("gliclazida")
                || item.contains("levotiroxina") || item.contains("tiamazol")) {
            return "Endocrino e Diabetes";
        }
        if (item.contains("sertralina") || item.contains("escitalopram") || item.contains("fluoxetina")
                || item.contains("venlafaxina") || item.contains("amitriptilina") || item.contains("quetiapina")
                || item.contains("risperidona") || item.contains("alprazolam") || item.contains("clonazepam")
                || item.contains("diazepam") || item.contains("bromazepam")) {
            return "Saude Mental";
        }
        if (item.contains("vitamina") || item.contains("complexo b") || item.contains("acido folico")
                || item.contains("carbonato de calcio")) {
            return "Vitaminas e Suplementos";
        }
        if (item.contains("protetor solar") || item.contains("shampoo") || item.contains("pomada")
                || item.contains("creme") || item.contains("locao") || item.contains("sabonete")
                || item.contains("condicionador") || item.contains("desodorante") || item.contains("hidratante")
                || item.contains("serum") || item.contains("repelente") || item.contains("fralda")
                || item.contains("absorvente") || item.contains("escova dental") || item.contains("fio dental")
                || item.contains("creme dental") || item.contains("antisseptico bucal") || item.contains("lenco")
                || item.contains("algodao") || item.contains("curativo") || item.contains("esparadrapo")
                || item.contains("atadura") || item.contains("gaze") || item.contains("barbear")
                || item.contains("vaselina") || item.contains("acetona") || item.contains("esmalte")
                || item.contains("talco") || item.contains("depilatorio") || item.contains("touca")
                || item.contains("papel higienico") || item.contains("cinta termica")
                || item.contains("bolsa termica") || item.contains("termometro")) {
            return "Dermocosmeticos";
        }
        if (item.contains("soro fisiologico") || item.contains("spray nasal") || item.contains("descongestionante")
                || item.contains("salbutamol") || item.contains("budesonida") || item.contains("nebulizacao")) {
            return "Respiratorio";
        }
        return "Clinica Geral";
    }

    private static String inferirLaboratorio(int indice) {
        String[] laboratorios = {
                "EMS", "Neo Quimica", "Medley", "Eurofarma", "Cimed", "Teuto",
                "AchÃ©", "Prati-Donaduzzi", "Germed", "Legrand"
        };
        return laboratorios[indice % laboratorios.length];
    }

    private static String inferirTipoControle(String nome) {
        String item = nome.toLowerCase();
        if (item.contains("alprazolam") || item.contains("clonazepam") || item.contains("diazepam")
                || item.contains("bromazepam") || item.contains("tramadol") || item.contains("pregabalina")
                || item.contains("carbamazepina") || item.contains("fenobarbital") || item.contains("fenitoina")
                || item.contains("quetiapina") || item.contains("risperidona") || item.contains("acido valproico")
                || item.contains("levodopa") || item.contains("memantina")) {
            return "Controlado";
        }
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("ciprofloxacino") || item.contains("claritromicina")
                || item.contains("metronidazol") || item.contains("levofloxacino")
                || item.contains("norfloxacino") || item.contains("sulfametoxazol")) {
            return "Antibiotico";
        }
        return "Livre";
    }

    private static int inferirEstoqueMinimo(String nome, int indice) {
        String item = nome.toLowerCase();
        if (item.contains("controlado")) {
            return 8;
        }
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("losartana") || item.contains("metformina") || item.contains("dipirona")
                || item.contains("paracetamol") || item.contains("ibuprofeno")) {
            return 20;
        }
        return 10 + (indice % 8);
    }

    private static String inferirClasseComercial(String nome, int indice) {
        String item = nome.toLowerCase();
        if (item.contains("creme") || item.contains("shampoo") || item.contains("pomada") || item.contains("locao")
                || item.contains("spray nasal") || item.contains("imecap") || item.contains("sabonete")
                || item.contains("condicionador") || item.contains("desodorante") || item.contains("hidratante")
                || item.contains("serum") || item.contains("repelente") || item.contains("fralda")
                || item.contains("absorvente") || item.contains("escova dental") || item.contains("fio dental")
                || item.contains("creme dental") || item.contains("antisseptico bucal") || item.contains("algodao")
                || item.contains("curativo") || item.contains("esparadrapo") || item.contains("atadura")
                || item.contains("gaze") || item.contains("barbear") || item.contains("vaselina")
                || item.contains("acetona") || item.contains("esmalte") || item.contains("talco")
                || item.contains("depilatorio") || item.contains("touca") || item.contains("papel higienico")
                || item.contains("cinta termica") || item.contains("bolsa termica") || item.contains("termometro")) {
            return "Perfumaria";
        }
        if (item.contains("amoxicilina") || item.contains("azitromicina") || item.contains("cefalexina")
                || item.contains("ciprofloxacino") || item.contains("claritromicina") || item.contains("norfloxacino")
                || item.contains("metronidazol") || item.contains("dipirona") || item.contains("paracetamol")
                || item.contains("ibuprofeno") || item.contains("losartana") || item.contains("metformina")
                || item.contains("atorvastatina") || item.contains("valsartana") || item.contains("ramipril")
                || item.contains("sitagliptina") || item.contains("vildagliptina") || item.contains("empagliflozina")
                || item.contains("dapagliflozina") || item.contains("nitazoxanida") || item.contains("mesalazina")
                || item.contains("sumatriptana") || item.contains("acetilcisteina") || item.contains("ambroxol")
                || item.contains("carbocisteina") || item.contains("cetoprofeno") || item.contains("ezetimiba")
                || item.contains("finasterida") || item.contains("nebivolol") || item.contains("olmesartana")
                || item.contains("glimepirida") || item.contains("levocetirizina") || item.contains("guaifenesina")) {
            return "Generico";
        }
        return indice % 3 == 0 ? "Original" : "Similar";
    }

    private static void sincronizarTipoControleProdutos(Connection conexao) throws SQLException {
        String sqlBusca = "SELECT id, nome, tipo_controle FROM produtos";
        String sqlAtualiza = "UPDATE produtos SET tipo_controle = ? WHERE id = ?";

        try (PreparedStatement busca = conexao.prepareStatement(sqlBusca);
             PreparedStatement atualiza = conexao.prepareStatement(sqlAtualiza);
             ResultSet rs = busca.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nome = rs.getString("nome");
                String tipoAtual = rs.getString("tipo_controle");
                String tipoCorreto = inferirTipoControle(nome == null ? "" : nome);
                if (tipoCorreto.equalsIgnoreCase(tipoAtual == null ? "" : tipoAtual)) {
                    continue;
                }
                atualiza.setString(1, tipoCorreto);
                atualiza.setInt(2, id);
                atualiza.addBatch();
            }
            atualiza.executeBatch();
        }
    }
}

