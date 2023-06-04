package application;

import java.awt.Label;
import java.beans.Statement;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;


import java.awt.Desktop;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class CRUDManager {
    private TableView<Etudiant> tableView;
    private ObservableList<Etudiant> data;

    private TextField nameField;
    private TextField ageField;
    private TextField addressField;
    private Text nametext;
    private Text agetext;
    private Text adressetext;
   
    

    public CRUDManager() {
        data = FXCollections.observableArrayList();
        tableView = createTableView();
        nameField = new TextField();
        ageField = new TextField();
        addressField = new TextField();
        nametext = new Text("Nom:");
        agetext = new Text("Age:");
        adressetext = new Text("Adresse:");
       
    }

    public GridPane getMainPane() {
      GridPane grid = new GridPane();
        grid.add(tableView,0,0,4,4);
     
        grid.add(nametext, 4, 1);
        grid.add(nameField,4,2 );
        grid.add(agetext, 6, 1);
        grid.add(ageField,6 , 2);
        grid.add(adressetext, 8, 1);
        grid.add(addressField,8, 2);
       
        Button addButton = new Button("Ajouter");
        grid.add(addButton,2,4);
        Button deleteButton = new Button("Supprimer");
        grid.add(deleteButton,2, 5);
        Button updateButton = new Button("Mettre à jour");
        grid.add(updateButton, 2, 6);
        Button viewButton = new Button("Voir profil");
        grid.add(viewButton, 2, 7);
        Button printButton = new Button("Imprimer");
        grid.add(printButton, 2, 8);
        

        addButton.setOnAction(e -> {
			addData();
		});
        deleteButton.setOnAction(e -> deleteData());
        updateButton.setOnAction(e -> updateData());
        viewButton.setOnAction(e -> viewProfile());
        printButton.setOnAction(e -> printSummarySheet() );

      
        return grid;
    }

    private TableView<Etudiant> createTableView() {
        TableView<Etudiant> tableView = new TableView<>();
        TableColumn<Etudiant, String> nameColumn = new TableColumn<>("Nom");
        TableColumn<Etudiant, Integer> ageColumn = new TableColumn<>("Âge");
        TableColumn<Etudiant, String> addressColumn = new TableColumn<>("Adresse");

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        tableView.getColumns().addAll(nameColumn, ageColumn, addressColumn);
        tableView.setItems(data);

        return tableView;
    }

    private void addData() {
    	 String name = nameField.getText().trim();
    	    String ageText = ageField.getText().trim();
    	    String address = addressField.getText().trim();

    	    if (!name.isEmpty() && !ageText.isEmpty() && !address.isEmpty()) {
    	        try {
    	            int age = Integer.parseInt(ageText);
    	            Etudiant student = new Etudiant(name, age, address);
	                data.add(student);


	                Connection connection = DBConnection.getConnection();
	                PreparedStatement statement = connection.prepareStatement("INSERT INTO etudiant (nom, age, adresse) VALUES (?, ?, ?)");
	                statement.setString(1, name);
	                statement.setInt(2, age);
	                statement.setString(3, address);

	                int rowsAffected = statement.executeUpdate();

	                if (rowsAffected > 0) {
	                    System.out.println("Les données ont été insérées avec succès.");
    	            } else {
    	                System.out.println("Échec de l'insertion des données.");
    	            }

    	           
    	            connection.close();
    	        } catch (NumberFormatException e) {
    	            showErrorAlert("L'âge doit être un nombre entier.");
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	            showErrorAlert("Une erreur s'est produite lors de l'insertion des données dans la base de données.");
    	        }
    	    } else {
    	        showErrorAlert("Veuillez remplir tous les champs.");
    	    }
    }


    private void deleteData() {
        Etudiant selectedStudent = tableView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation de suppression");
            confirmation.setHeaderText(null);
            confirmation.setContentText("Voulez-vous vraiment supprimer cet étudiant ?");
            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                data.remove(selectedStudent);

                try {
                    Connection connection = DBConnection.getConnection();
                    PreparedStatement statement = connection.prepareStatement("DELETE FROM etudiant WHERE Age = ?");
                    statement.setInt(1, selectedStudent.getAge());
                    statement.executeUpdate();

                    statement.close();
                    connection.close();

                    System.out.println("L'étudiant a été supprimé avec succès de la base de données.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showErrorAlert("Une erreur s'est produite lors de la suppression de l'étudiant de la base de données.");
                }
            }
        } else {
            showErrorAlert("Veuillez sélectionner un étudiant.");
        }
    }


    private void updateData() {
        Etudiant selectedStudent = tableView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String address = addressField.getText().trim();

            if (!name.isEmpty() && !ageText.isEmpty() && !address.isEmpty()) {
                try {
                    int age = Integer.parseInt(ageText);

                    selectedStudent.setNom(name);
                    selectedStudent.setAge(age);
                    selectedStudent.setAdresse(address);

                    tableView.refresh();

                    try {
                        Connection connection = DBConnection.getConnection();
                        PreparedStatement statement = connection.prepareStatement("UPDATE etudiant SET age = ?, adresse = ? WHERE nom = ?");
                        statement.setInt(1, age);
                        statement.setString(2, address);
                        statement.setString(3, name);
                        statement.executeUpdate();

                        statement.close();
                        connection.close();

                        System.out.println("L'étudiant a été mis à jour avec succès dans la base de données.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showErrorAlert("Une erreur s'est produite lors de la mise à jour de l'étudiant dans la base de données.");
                    }

                    clearFields();
                } catch (NumberFormatException e) {
                    showErrorAlert("L'âge doit être un nombre entier.");
                }
            } else {
                showErrorAlert("Veuillez remplir tous les champs.");
            }
        } else {
            showErrorAlert("Veuillez sélectionner un étudiant.");
        }
    }




    private void viewProfile() {
        Etudiant selectedStudent = tableView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Profil étudiant");
            alert.setHeaderText(null);
            alert.setContentText("Nom: " + selectedStudent.getNom() +
                    "\nÂge: " + selectedStudent.getAge() +
                    "\nAdresse: " + selectedStudent.getAdresse());
            alert.showAndWait();
        } else {
            showErrorAlert("Veuillez sélectionner un étudiant.");
        }
    }
    private void printSummarySheet() {
        Etudiant selectedStudent = tableView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la fiche récapitulative");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                try (PDDocument document = new PDDocument()) {
                    PDPage page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    PDPageContentStream contentStream = new PDPageContentStream(document, page);
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText("Fiche Etudiant(e)");
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(0, -30);
                    contentStream.showText("Nom: " + selectedStudent.getNom());
                    contentStream.newLineAtOffset(0, -40);
                    contentStream.showText("Âge: " + selectedStudent.getAge());
                    contentStream.newLineAtOffset(0, -50);
                    contentStream.showText("Adresse: " + selectedStudent.getAdresse());
                    contentStream.endText();
                    contentStream.close();

                    document.save(file);
                    document.close();

                    
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    showErrorAlert("Une erreur s'est produite lors de l'enregistrement du fichier.");
                }
            }
        } else {
            showErrorAlert("Veuillez sélectionner un étudiant.");
        }
    }
    
    private void clearFields() {
        nameField.clear();
        ageField.clear();
        addressField.clear();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}