import SwiftUI
import shared

struct UserNotFoundView: View {
    @Binding var navigationPath: NavigationPath
    @EnvironmentObject private var viewModel: ProfileViewModel
    
    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    
    var body: some View {
        ZStack {
            // Fondo amarillo
            customYellow
                .edgesIgnoringSafeArea(.all)
            
            // Contenido principal
            VStack(spacing: 24) {
                // Mensaje de error
                Text("USER NOT FOUND")
                    .font(.system(size: 32, weight: .bold))
                    .foregroundColor(.black)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
                
                Text("The user you searched for doesn't exist or couldn't be found.")
                    .font(.system(size: 18))
                    .foregroundColor(.black)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
            
            // Botón para volver
            VStack {
                HStack {
                    Button(action: {
                        viewModel.clearSearch()
                        navigationPath.removeLast()
                    }) {
                        Image(systemName: "arrow.backward")
                            .foregroundColor(.black)
                            .padding()
                            .background(
                                Circle()
                                    .fill(customYellow)
                                    .shadow(radius: 2)
                            )
                    }
                    .padding(.leading, 16)
                    .padding(.top, 16)
                    
                    Spacer()
                }
                Spacer()
            }
        }
        .navigationBarHidden(true)
    }
}
