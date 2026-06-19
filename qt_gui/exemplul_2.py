import os
import sys
from PyQt5.QtWidgets import QWidget, QApplication, QFileDialog, QMessageBox, QVBoxLayout, QLabel, QLineEdit, \
    QPushButton, QButtonGroup
from PyQt5 import QtCore
from PyQt5.uic import loadUi
from mq_communication import RabbitMq


class AddBookDialog(QWidget):
    def __init__(self, parent_app):
        super(AddBookDialog, self).__init__()
        self.parent_app = parent_app  # Păstrăm o referință către aplicația principală pentru a trimite mesaje
        self.init_ui()

    def init_ui(self):
        self.setWindowTitle('Introducere Carte Nouă')
        self.resize(350, 250)

        # Layout vertical simplu și curat format programmatic
        layout = QVBoxLayout()

        layout.addWidget(QLabel('Autor:'))
        self.author_input = QLineEdit(self)
        layout.addWidget(self.author_input)

        layout.addWidget(QLabel('Titlu / Denumire:'))
        self.title_input = QLineEdit(self)
        layout.addWidget(self.title_input)

        layout.addWidget(QLabel('Editură:'))
        self.publisher_input = QLineEdit(self)
        layout.addWidget(self.publisher_input)

        layout.addWidget(QLabel('Conținut text:'))
        self.text_input = QLineEdit(self)
        layout.addWidget(self.text_input)

        self.save_btn = QPushButton('Salvează în Bibliotecă', self)
        self.save_btn.setStyleSheet("background-color: green; color: white; font-weight: bold;")
        self.save_btn.clicked.connect(self.submit_data)
        layout.addWidget(self.save_btn)

        # NOTĂ: Liniile de QButtonGroup au fost șterse complet de aici, fiindcă ele aparțin exclusiv ferestrei principale!

        self.setLayout(layout)

    def submit_data(self):
        author = self.author_input.text().strip()
        title = self.title_input.text().strip()
        publisher = self.publisher_input.text().strip()
        text = self.text_input.text().strip()

        if not (author and title and publisher and text):
            QMessageBox.warning(self, 'Eroare', 'Toate câmpurile sunt obligatorii!')
            return

        # Construim string-ul în formatul add:autor;continut;titlu;editura pe care îl așteaptă Kotlin
        request = f"add:{author};{text};{title};{publisher}"

        # Trimitem prin instanța de rabbit_mq a ferestrei părinte
        self.parent_app.send_request(request)

        QMessageBox.information(self, 'Succes', 'Cererea de adăugare a fost trimisă!')
        self.close()  # Închidem fereastra după trimitere


def debug_trace(ui=None):
    from pdb import set_trace
    QtCore.pyqtRemoveInputHook()
    set_trace()


class LibraryApp(QWidget):
    ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

    def __init__(self):
        super(LibraryApp, self).__init__()
        ui_path = os.path.join(self.ROOT_DIR, 'exemplul_2.ui')
        loadUi(ui_path, self)

        # --- REZOLVARE BUTOANE RADIO LOGIC (Aici este locul lor perfect) ---
        self.format_group = QButtonGroup(self)
        self.format_group.addButton(self.json_rb)
        self.format_group.addButton(self.html_rb)
        if hasattr(self, 'xml_rb'):
            self.format_group.addButton(self.xml_rb)

        self.search_group = QButtonGroup(self)
        self.search_group.addButton(self.author_rb)
        self.search_group.addButton(self.title_rb)
        if hasattr(self, 'publisher_rb'):
            self.search_group.addButton(self.publisher_rb)
        # ------------------------------------------------------------------

        self.search_btn.clicked.connect(self.search)
        self.save_as_file_btn.clicked.connect(self.save_as_file)
        try:
            self.add_book_btn.clicked.connect(self.open_add_book_window)
        except AttributeError:
            print("Avertisment: ad_book_btn nu a fost găsit în fișierul .ui încă.")

        self.rabbit_mq = RabbitMq(self)
        self.add_window = None

    def set_response(self, response):
        self.result.setText(response)

    def send_request(self, request):
        self.rabbit_mq.send_message(message=request)
        self.rabbit_mq.receive_message()

    def open_add_book_window(self):
        # Deschierea ferestrei noi asincrone
        self.add_window = AddBookDialog(self)
        self.add_window.show()

    def search(self):
        search_string = self.search_bar.text()
        request = None
        if not search_string:
            if self.json_rb.isChecked():
                request = 'print:json'
            elif self.html_rb.isChecked():
                request = 'print:html'
            elif hasattr(self, 'xml_rb') and self.xml_rb.isChecked():
                request = 'print:xml'
            else:
                request = 'print:raw'
        else:
            if self.author_rb.isChecked():
                request = 'find:author={}'.format(search_string)
            elif self.title_rb.isChecked():
                request = 'find:title={}'.format(search_string)
            else:
                request = 'find:publisher={}'.format(search_string)
        self.send_request(request)

    def save_as_file(self):
        options = QFileDialog.Options()
        options |= QFileDialog.DontUseNativeDialog

        # Preluăm calea direct descompunând tuplul (cale, filtru) evită erorile de formatare
        file_path, _ = QFileDialog.getSaveFileName(self, 'Salvare fisier', "", "All Files (*)", options=options)

        if file_path:
            # Sincronizare automată extensie în funcție de opțiune
            if not any(file_path.endswith(ext) for ext in ['.json', '.html', '.txt', '.xml']):
                if self.json_rb.isChecked():
                    file_path += '.json'
                elif self.html_rb.isChecked():
                    file_path += '.html'
                elif hasattr(self, 'xml_rb') and self.xml_rb.isChecked():
                    file_path += '.xml'
                else:
                    file_path += '.txt'
            try:
                # Citim textul inteligent: dacă result e un QTextEdit folosește toPlainText/toHtml, altfel folosește .text() dacă e un QLabel
                if hasattr(self.result, 'toPlainText'):
                    text_de_salvat = self.result.toHtml() if file_path.endswith(".html") else self.result.toPlainText()
                else:
                    text_de_salvat = self.result.text()

                with open(file_path, 'w', encoding='utf-8') as fp:
                    fp.write(text_de_salvat)

                QMessageBox.information(self, 'Exemplul 2', 'Fișierul a fost salvat cu succes!')
            except Exception as e:
                print(f"Eroare detaliata la salvare: {e}")
                QMessageBox.warning(self, 'Exemplul 2', f'Nu s-a putut salva fisierul. Eroare: {e}')


if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = LibraryApp()
    window.show()
    sys.exit(app.exec_())