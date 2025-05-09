import SwiftUI
import shared

struct WelcomeView: View {
    @EnvironmentObject private var viewModel: ProfileViewModel
    @Binding var navigationPath: NavigationPath
    
    @State private var searchQuery = ""
    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let avatarSize: CGFloat = 150
    private let buttonHeight: CGFloat = 50
    
    var body: some View {
        ZStack {
            // Fondo negro
            Color.black
                .edgesIgnoringSafeArea(.all)
            
            // Contenido principal
            VStack(spacing: 0) {
                // Header (Avatar + Welcome)
                headerSection
                    .padding(.top, 32)
                
                Spacer()
                
                // Search Section
                searchSection
                    .padding(.bottom, 32)
            }
            .padding(.horizontal, 16)
            
            // Loading overlay
            if case .loading = viewModel.searchState {
                loadingOverlay
            }
        }
        .navigationBarHidden(true)
    }
    
    // MARK: - Componentes
    
    private var headerSection: some View {
        VStack(spacing: 24) {
            Text("Welcome \(SessionManager.shared.user_login ?? "")!")
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(customYellow)
            
            AsyncImage(
                url: URL(string: SessionManager.shared.user_image_url ?? ""),
                content: { image in
                    image.resizable()
                        .aspectRatio(contentMode: .fill)
                },
                placeholder: {
                    ProgressView()
                }
            )
            .frame(width: avatarSize, height: avatarSize)
            .clipShape(Circle())
            .overlay(
                Circle()
                    .stroke(customYellow, lineWidth: 2)
            )
        }
    }
    
    
    private var searchSection: some View {
        VStack(spacing: 24) {
            // Search Field - Corregido
            HStack {
                TextField("Search user...", text: $searchQuery)
                    .foregroundColor(.white)
                    .padding(8)
                    .background(
                        RoundedRectangle(cornerRadius: 16)
                            .fill(Color.black.opacity(0.2))
                    )
                    .overlay(
                        RoundedRectangle(cornerRadius: 16)
                            .stroke(customYellow.opacity(0.5), lineWidth: 1)
                    )
                
                Button(action: {
                    viewModel.searchForUser(login: searchQuery)
                }) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(searchQuery.isEmpty ? .gray : customYellow)
                }
                .disabled(searchQuery.isEmpty)
            }
            .padding(.horizontal, 12)
            
            // My Profile Button - Corregido
            Button(action: {
                if let myLogin = SessionManager.shared.user_login {
                    viewModel.searchForUser(login: myLogin)
                }
            }) {
                Text("MY PROFILE")
                    .font(.system(size: 20, weight: .bold))
                    .frame(maxWidth: .infinity)
                    .padding()
                    .foregroundColor(customYellow)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(customYellow, lineWidth: 2)
                    )
            }
            
            Spacer()
                        
            // Logout Button
            Button(action: logout) {
                Text("LOG OUT")
                    .font(.system(size: 16, weight: .black))
                    .frame(width: 110, height: 110)
                    .background(customYellow)
                    .foregroundColor(.black)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(Color.black, lineWidth: 2)
                    )
            }
            .padding(.bottom, 16)
        }
    }
    
    private var loadingOverlay: some View {
        Color.black.opacity(0.7)
            .edgesIgnoringSafeArea(.all)
            .overlay(
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: customYellow))
                    .scaleEffect(2)
            )
    }
    
    // MARK: - Lógica
    
    private func logout() {
        viewModel.resetState()
        SessionManager.shared.clearSession()
        navigationPath.removeLast(navigationPath.count)
        navigationPath.append("login")
    }
}

// Preview
struct WelcomeView_Previews: PreviewProvider {
    static var previews: some View {
        let mockVM = ProfileViewModel()
        mockVM.authState = .success(authData: AuthData(
            access_token: "",
            refresh_token: "",
            userId: 123,
            userLogin: "ejemplo",
            imageUrl: "https://cdn.intra.42.fr/users/ejemplo.jpg"
        ))
        
        return WelcomeView(navigationPath: .constant(NavigationPath()))
            .environmentObject(mockVM)
    }
}
