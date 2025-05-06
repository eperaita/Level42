import SwiftUI
import shared

struct SelectedProjectView: View {
    let project: Project
    @Binding var navigationPath: NavigationPath

    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let customBlack = Color.black

    var body: some View {
        ZStack {
            // Fondo amarillo
            customYellow.ignoresSafeArea()

            // Contenido principal
            if project.project.id != 0 {
                VStack(spacing: 0) {  // 1. Cambiado a spacing: 0 para control manual
                    // 2. Añadido Spacer para bajar el círculo
                    Spacer()
                        .frame(height: UIScreen.main.bounds.height * 0.2) // 10% de la pantalla

                    // Círculo negro (posición ajustada)
                    ZStack {
                        Circle()
                            .fill(customBlack)
                            .frame(width: 220, height: 220)

                        Text(project.project.name)
                            .font(.system(size: 28, weight: .bold))
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)
                            .padding(20)
                            .fixedSize(horizontal: false, vertical: true) // 3. Para texto largo
                    }

                    // 4. Spacer entre círculo y texto (20 puntos)
                    Spacer()
                        .frame(height: 20)

                    // Detalles del proyecto (centrados)
                    VStack(alignment: .center, spacing: 12) {
                        if let finalMark = project.finalMark, finalMark != 0 {
                            Text("Final Mark: \(finalMark)")
                                .font(.system(size: 20))
                                .foregroundColor(customBlack)
                                .multilineTextAlignment(.center)
                        }

                        Text("Status: \(project.status)")
                            .font(.system(size: 20))
                            .foregroundColor(customBlack)
                            .multilineTextAlignment(.center)

                        Text("Updated At: \(project.updatedAt)")
                            .font(.system(size: 20))
                            .foregroundColor(customBlack)
                            .multilineTextAlignment(.center)
                    }
                    .padding(.horizontal, 32)

                    // 5. Spacer inferior para centrar el conjunto
                    Spacer()
                }
            } else {
                Text("Proyecto no encontrado")
                    .font(.system(size: 20))
                    .foregroundColor(.red)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }

            // Botón atras
            Button(action: {
                navigationPath.removeLast()
            }) {
                Image(systemName: "chevron.left")
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(customYellow)
                    .frame(width: 50, height: 50)
                    .background(customBlack)
                    .clipShape(Circle())
            }
            .padding(.leading, 16)
            .padding(.top, 16)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        }
        .navigationBarBackButtonHidden(true)
        .navigationBarHidden(true)
    }
}