from tkinter import *
from tkinter import ttk
import threading
import socket
from tkinter.ttk import Combobox

HOST = "localhost"
CLIENT_PORT = 1700 # trimit intrebarea la CLIENT
LIVRARE_PORT = 1701 # si primesc raspuns de la LIVRARE

def get_answer():
    # creare socket TCP
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    # incercare de conectare catre microserviciul LIVRARE
    try:
        sock.connect((HOST, LIVRARE_PORT))

        # primire raspuns -> microserviciul LIVRARE trimite
        # raspunsul inapoi
        response_text = str(sock.recv(1024), "utf-8")
        return response_text

    except ConnectionError:
        # in cazul unei erori de conexiune, se afiseaza un
        # mesaj
        response_text = ("Eroare de conectare la "
                         "microserviciul LIVRARE!")
        return response_text

def send_question(question_text):
    # creare socket TCP
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    response_text = "Eroare necunoscuta." # mesaj default

    # incercare de conectare catre microserviciul CLIENT
    try:
        sock.connect((HOST, CLIENT_PORT))

        # transmitere intrebare - se deleaga intrebarea catre
        # microserviciu
        sock.send(bytes(question_text + "\n", "utf-8"))

        # primire raspuns -> microserviciul LIVRARE trimite
        # raspunsul inapoi
        response_text = get_answer()

    except ConnectionError:
        # in cazul unei erori de conexiune, se afiseaza un
        # mesaj
        response_text = ("Eroare de conectare la "
                         "microserviciul CLIENT!")

    finally:
        sock.close()

    # se adauga raspunsul primit in caseta text din interfata
    # grafica
    response_widget.insert(END, response_text)

def ask_question():
    # preluare text intrebare de pe interfata grafica
    order_text = f"{name.get()}|{adress.get()}|{cb.get()}|{quantity.get()}"

    # pornire thread separat pentru tratarea intrebarii
    # respective
    # astfel, nu se blocheaza interfata grafica!
    threading.Thread(target=send_question,
                     args=(order_text,)).start()

if __name__ == '__main__':
    # elementul radacina al interfetei grafice
    root = Tk()
    root.title("Interactiune profesor-studenti")

    # la redimensionarea ferestrei, cadrele se extind pentru
    # a prelua spatiul ramas
    root.columnconfigure(0, weight=1)
    root.rowconfigure(0, weight=1)

    # cadrul care incapsuleaza intregul continut
    content = ttk.Frame(root)

    # caseta text care afiseaza raspunsurile la intrebari
    response_widget = Text(content, height=10, width=50)

    # eticheta text din partea dreapta
    name_label = ttk.Label(content, text="Nume:")

    # caseta de introducere text cu care se preia intrebarea de
    # la utilizator
    name = ttk.Entry(content, width=50)

    # eticheta text din partea dreapta
    adress_label = ttk.Label(content, text="Adresa:")

    # caseta de introducere text cu care se preia intrebarea de
    # la utilizator
    adress = ttk.Entry(content, width=50)

    # dropdown Produse
    # momentan default
    # TODO de facut bd-ul si de generat produse din ea
    produse = ["Masca protectie", "Combinezon",
               "Manusa chirurgicala"]
    cb = Combobox(content, values=produse)

    quantity = ttk.Entry(content, width=20)

    # butoanele

    # la apasare, se apeleaza functia ask_question
    sendbtn = ttk.Button(content, text="Trimite comanda",
                     command=ask_question)

    # la apasare, se iese din aplicatie
    exitbtn = ttk.Button(content, text="Iesi",
                         command=root.destroy)

    # plasarea elementelor in layout-ul de tip grid
    content.grid(column=0, row=0)
    response_widget.grid(column=0, row=1, columnspan=2,
                         rowspan=2)

    name_label.grid(column=2, row=0)
    name.grid(column=2, row=1)

    adress_label.grid(column=3, row=0)
    adress.grid(column=3, row=1)

    cb.grid(column=1, row=2, columnspan=2)

    quantity.grid(column=2, row=2, columnspan=2)

    sendbtn.grid(column=2, row=3)
    exitbtn.grid(column=3, row=3)

    # bucla principala a interfetei grafice care
    # asteapta evenimente de la utilizator
    root.mainloop()